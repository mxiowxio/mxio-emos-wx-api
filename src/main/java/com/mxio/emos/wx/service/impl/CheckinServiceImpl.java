package com.mxio.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.mxio.emos.wx.config.SystemConstants;
import com.mxio.emos.wx.db.mapper.*;
import com.mxio.emos.wx.db.pojo.TbCheckinPo;
import com.mxio.emos.wx.db.pojo.TbFaceModelPo;
import com.mxio.emos.wx.exception.EmosException;
import com.mxio.emos.wx.service.CheckinService;
import com.mxio.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author mxio
 */

@Service
@Slf4j
@Scope("prototype")//以后异步进程，多线程，发送邮件
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbHolidaysMapper holidaysDao;

    @Autowired
    private TbCheckinMapper checkinDao;

    @Autowired
    private TbWorkdayMapper workdayDao;

    @Autowired
    private TbFaceModelMapper faceModelDao;

    @Autowired
    private TbCityMapper CityDao;

    @Autowired
    private TbUserMapper userDao;

    @Autowired
    private TbDeptMapper deptDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.code}")
    private String code;

    @Autowired
    private EmailTask emailTask;

    /**
     * 6-9 封装检测当天是否可以签到（业务层）-
     * 先判断当天能不能签到（是不是工作日（特殊的节假日？））,holidaysDao，workdayDao，以及正常判断日常，三方面判断当天能不能签到
     * 如果是工作日，再判断当前可不可以签到（太早，太迟，或者已经签到过了？）//SystemConstants，start，end判断时间点
     */
    @Override
    public String validCanCheckIn(int userId, String date) {

        //不为空true，bool_1为true说明是假期
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() != null ? true : false;
        //不为空true，bool_2为true说明是工作日
        boolean bool_2 = workdayDao.searchTodayIsWorkday() != null ? true : false;

        String type = "工作日";

        //判断当前是不是是周末，if（是，是周末）
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        //不单单上面
        // if (DateUtil.date().isWeekend())
        // 还要依靠下面来确定是不是特殊的工作日，节假日，另外的if elseif
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }

        if (type.equals("节假日")) {
            return "今天是节假日，节假日不需要考勤哦~";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;

            //start，end转化为DateTime，DateTime的比较
            // 将日期字符串转化为日期。parse：解析
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)) {
                return "还没开始考勤呢~";
            } else if (now.isAfter(attendanceEnd)) {
                return "很抱歉，上班考勤时间已截至！";
            }
            //没有太早，太迟，再判断之前有没有签到过了
            else {
                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("date", date);
                map.put("start", start);
                map.put("end", end);
                //是否是不是空的，不是空的，说明签到过了
                //bool，true，签到过了
                boolean bool = checkinDao.haveCheckin(map) != null ? true : false;
                return bool ? "今日已考勤，不用重复考勤哦~" : "可以考勤！";
            }
        }
    }

    /**
     * 时间->地区风险->->->
     */
    @Override
    public void checkin(HashMap param) {
        // 当前时间
        Date d1 = DateUtil.date();
        // 上班时间
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        // 考勤结束时间
        DateTime d3 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);

        //打卡开始之前，1表示打卡成功，2表示迟到了，之外就是矿工，超过attendanceEndTime
        int status = 1;
        // 开始之前
        if (d1.compareTo(d2) <= 0) {
            status = 1;
            //开始之后，结束之前
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {
            status = 2;
        } else {
            throw new EmosException("超出考勤时间段，无法考勤");
        }
        // 根据hash中取出数据userid
        int userId = (Integer) param.get("userId");
        // todo 查询疫情风险等级
        //  1是低风险，2中风险，3高风险
        int risk = 1;
        String city = (String) param.get("city");
        String district = (String) param.get("district");
        String address = (String) param.get("address");
        String country = (String) param.get("country");
        String province = (String) param.get("province");
        log.info("city:", city);
        log.info("district", district);
        log.info("address", address);
        log.info("country", country);
        log.info("province", province);
        // 还要将地区的code传入html中，获取html里面的信息，作判断是不是高风险地区
        /*if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
            String code = CityDao.searchCode(city);
            // jsoup获取get->elements->elements中读取第一个->elements第一个里面最后一个p标签内容->对p标签内容判断
            try {
                String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                Document document = Jsoup.connect(url).get();
                Elements elements = document.getElementsByClass("list-content");
                if (elements.size() > 0) {
                    Element element = elements.get(0);
                    String result = element.select("p:last-chile").text();
                    //高风险，需要发送邮件
                    //                            result="高风险";
                    //如果是高风险地区，就发送邮件
                    //result = "高风险";
                    if ("高风险".equals(result)) {
                        risk = 3;
                        //发送告警邮件
                        HashMap<String, String> map = userDao.searchNameAndDept(userId);
                        //tb_dept表中的dept_name
                        String name = map.get("name");
                        String deptName = map.get("dept_name");
                        deptName = deptName != null ? deptName : "";
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(hrEmail);
                        message.setSubject("员工" + name + "身处高风险疫情地区警告");
                        message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                        emailTask.sendAsync(message);
                    } else if ("中风险".equals(result)) {
                        risk = 2;
                    }
                }
            } catch (Exception e) {
                log.error("执行异常！", e);
                throw new EmosException("获取风险等级失败！");
            }
        }*/
        // todo 保存签到记录
        //  这里只要对比到人脸模型就存入数据，不一定是在作判断是不是高风险地区里面的if中存入，
        //  以后可能不需要用到那部分了，但数据还是要存入的
        /*String address = (String) param.get("address");
        String country = (String) param.get("country");
        String province = (String) param.get("province");*/
        TbCheckinPo entity = new TbCheckinPo();
        entity.setUserId(userId);
        entity.setAddress(address);
        entity.setCountry(country);
        entity.setProvince(province);
        entity.setCity(city);
        entity.setDistrict(district);
        entity.setStatus((byte) status);
        entity.setRisk(risk);
        entity.setDate(DateUtil.today());
        entity.setCreateTime(d1);
        checkinDao.insert(entity);
        // 之前写的sql语句，根据userid查人脸模型
        /*String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel == null) {
            throw new EmosException("不存在人脸模型！");
            // 如果存在人脸模型，通过在centos中的python人脸模型对比，多种情况对比结果
        } else {
            // 人脸模型 -> txt，放在path中
            String path = (String) param.get("path");
            //向python，centos发送http请求
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            // 前半部分拍照的人脸模型，后半部分faceModel数据库中的人脸模型
            request.form("photo", FileUtil.file(path), "targetModel", faceModel);
            request.form("code", code);
            HttpResponse response = request.execute();
            if (response.getStatus() != 200) {
                log.error("人脸识别服务异常！");
                throw new EmosException("人脸识别服务异常！");
            }
            String body = response.body();
            if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
                throw new EmosException(body);
            } else if ("False".equals(body)) {
                throw new EmosException("签到无效，非本人签到！");
                // 对比成功，还要获取hash中的其他数据，为之后存入保存签到记录TbCheckin实体保存数据
            } else if ("True".equals(body)) {
                // todo 查询疫情风险等级
                //  1是低风险，2中风险，3高风险
                int risk = 1;
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                // 还要将地区的code传入html中，获取html里面的信息，作判断是不是高风险地区
                if (!StrUtil.isBlank(city) && !StrUtil.isBlank(district)) {
                    String code = CityDao.searchCode(city);
                    // jsoup获取get->elements->elements中读取第一个->elements第一个里面最后一个p标签内容->对p标签内容判断
                    try {
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                        Document document = Jsoup.connect(url).get();
                        Elements elements = document.getElementsByClass("list-content");
                        if (elements.size() > 0) {
                            Element element = elements.get(0);
                            String result = element.select("p:last-chile").text();
                            //高风险，需要发送邮件
                            //                            result="高风险";
                            //如果是高风险地区，就发送邮件
                            //result = "高风险";
                            if ("高风险".equals(result)) {
                                risk = 3;
                                //发送告警邮件
                                HashMap<String, String> map = userDao.searchNameAndDept(userId);
                                //tb_dept表中的dept_name
                                String name = map.get("name");
                                String deptName = map.get("dept_name");
                                deptName = deptName != null ? deptName : "";
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");
                                message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                emailTask.sendAsync(message);
                            } else if ("中风险".equals(result)) {
                                risk = 2;
                            }
                        }
                    } catch (Exception e) {
                        log.error("执行异常！", e);
                        throw new EmosException("获取风险等级失败！");
                    }
                }
                // todo 保存签到记录
                //  这里只要对比到人脸模型就存入数据，不一定是在作判断是不是高风险地区里面的if中存入，
                //  以后可能不需要用到那部分了，但数据还是要存入的
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                TbCheckinPo entity = new TbCheckinPo();
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(district);
                entity.setStatus((byte) status);
                entity.setDate(DateUtil.today());
                entity.setCreateTime(d1);
                checkinDao.insert(entity);
            }*/
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long days = checkinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        // 首先查询用户本周的考勤情况，包括本周的特殊的工作日和节假日
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        // 查询本周特殊工作日和节假日
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(param);
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);
        // 生成本周七天的日期对象，起始日期(startDate)和结束日期(endDate)
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        // 生成日期对象
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list = new ArrayList<>();
        // 拿日期对象看当天是工作日还是节假日，如果是工作日则查看当天的考勤情况
        range.forEach(one -> {
            // 取当前日期对象的日期字符串
            String date = one.toString("yyyy-MM-dd");
            // 判定当前这一天是工作日还是节假日
            String type = "工作日";
            if (one.isWeekend()) {  // 先利用周末进行判断
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {  // 查询是否是特殊节假日
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {   // 查询是否是特殊工作日
                type = "工作日";
            }
            // 查看考勤结果
            String status = ""; // 定义空字符串的目的是将未来的日期不设定为旷工，因为考勤还未开始或者员工还没考勤
            // 判断当前是否是工作日 且 判断当天是否已经发生
            //
            // DateUtil.compare：
            //      第一个参数(one)：代表本周的某一天
            //      第二个参数(DateUtil.date())：代表当前这一天
            //      如果返回<=0,则是已经发生的这一天，我们要查询考勤结果，反之为空字符串
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status = "缺勤";
                boolean flag = false;
                // 如果能查到当前的考勤结果，则取出，反之则为缺勤旷工
                for (HashMap<String, String> map : checkinList) {
                    // 查看map是否含有当前日期，如果有则取出来
                    if (map.containsValue(date)) {
                        // 取出考勤结果
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                }
                // 如果当天的考勤未结束(员工还没想起来要打卡)
                // 把当天打卡的结束时间封装成一个日期对象
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
                String today = DateUtil.today();    // 当前日期的字符串
                // 判断当前时间是否早于打卡结束时间(员工未打卡而且打卡还未结束)
                if (date.equals(today) && DateUtil.date().isBefore(endTime) && !flag) {
                    status = "";
                }
            }
            // 将返回结果封装成ArrayList中返回
            HashMap map = new HashMap();
            // 当前日期
            map.put("date", date);
            // 打卡结果
            map.put("status", status);
            // 当前这一天是工作日还是节假日
            map.put("type", type);
            // 当前这一天是周几
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            // 将所有结果放入list
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}

    /*@Override
    public String searchDeptName(int deptId) {
        String deptName = deptDao.searchDeptName(deptId);
        return deptName;
    }*/

    /*@Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(path));
        request.form("code", code);
        HttpResponse response = request.execute();
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException(body);
        } else {
            TbFaceModelPo entity = new TbFaceModelPo();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelDao.insert(entity);
        }
    }
*/

