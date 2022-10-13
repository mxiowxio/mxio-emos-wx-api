package com.mxio.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.mxio.emos.wx.common.util.R;
import com.mxio.emos.wx.config.SystemConstants;
import com.mxio.emos.wx.config.shiro.JwtUtil;
import com.mxio.emos.wx.controller.form.CheckinForm;
import com.mxio.emos.wx.controller.form.SearchMonthCheckinForm;
import com.mxio.emos.wx.db.mapper.TbCheckinMapper;
import com.mxio.emos.wx.exception.EmosException;
import com.mxio.emos.wx.service.CheckinService;
import com.mxio.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author mxio
 */

@RequestMapping("checkin")
@RestController
@Slf4j
@Api("签到模块web接口")
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @GetMapping("validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }


    @PostMapping("checkin")
    @ApiOperation("签到")
    public R checkin(@Valid @RequestBody CheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("city", form.getCity());
        param.put("district", form.getDistrict());
        param.put("address", form.getAddress());
        param.put("country", form.getCountry());
        param.put("province", form.getProvince());
        log.info("from:{}",form);
        checkinService.checkin(param);
        return R.ok("签到成功");

        /*if (file == null) {
            return R.error("没有上传文件！");
        }
        // 根据token，jwtutil解析token找到userid
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片！");
        } else {
            String path = imageFolder + "/" + fileName;
            try {
                // 将数据传到数据库中，form表单
                file.transferTo(Paths.get(path));
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("path", path);
                param.put("city", form.getCity());
                param.put("district", form.getDistrict());
                param.put("address", form.getAddress());
                param.put("country", form.getCountry());
                param.put("province", form.getProvince());
                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误！");
            } finally {
                FileUtil.del(path);
            }
        }*/
    }

    /*@PostMapping("createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传文件！");
        }
        // 根据token，jwtutil解析token找到userid
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式的图片！");
        } else {
            String path = imageFolder + "/" + fileName;
            try {
                // 将数据传到数据库中，form表单
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误！");
            } finally {
                FileUtil.del(path);
            }
        }
    }*/

    /**
     * 查询用户其拿到情况
     * @param token
     * @return
     */
    @GetMapping("searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public HashMap searchTodayCheckin(@RequestHeader("token") String token) {

        // 通过令牌解析出userId
        int userId = jwtUtil.getUserId(token);

        // 利用userId查询员工当天的考勤结果（里边包括员工的基本信息）
        HashMap map = checkinService.searchTodayCheckin(userId);
        // 往map中放入当天的考勤开始时间的结束时间
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        // 查询员工签到了多少天
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        // 判断员工是否在用户入职之前
        // 利用userId获取员工的入职日期，封装成日期对象。(获取的日期是字符串类型，字符串无法比较日期大小，所以得封装成日期对象)
        DateTime hireDate = DateUtil.parse(userService.searchUserHiredate(userId));
        // 获取当前这一周开始的日期对象
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        // 判断(如果开始日期在入职日期之前，则不考虑，只考虑入职日期之后的考勤记录)
        if (startDate.isBefore(hireDate)) {
            startDate = hireDate;
        }
        // 获取本周的结束日期
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        // 将开始日期的结束日期放入hashMap
        HashMap param = new HashMap();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        // 将map传给查询方法来查询本周的考勤结果
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        // 将考勤结果放入上方的map中(不是param)
        map.put("weekCheckin", list);
        // 将考勤结果返回
        return R.ok().put("result", map);

    }


    @PostMapping("searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        // 获取用户userId
        int userId = jwtUtil.getUserId(token);
        // 查询员工入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        // 把月份处理成双数字
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        // 某年某月的起始日期
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        // 如果查询的月份早于员工入职的日期的月份就抛异常
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("只能查询考勤之后日期的数据");
        }
        // 如果查询月份于入职月份恰好是同日，本月考勤查询开始日期设置成入职日期
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap<String, String> one : list) {
            // 知道当天是什么类型
            String type = one.get("type");
            // 保存当天这一天的考勤结果
            String status = one.get("status");
            // 判断考勤状态
            if ("工作日".equals(type)) {
                if ("正常".equals(status)) {
                    sum_1++;
                }
                else if ("迟到".equals(status)) {
                    sum_2++;
                }
                else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        // 统计当月考勤的各种天数
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }

}
