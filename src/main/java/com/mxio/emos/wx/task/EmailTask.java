package com.mxio.emos.wx.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author mxio
 */

@Component
@Scope("prototype")
@Slf4j
public class EmailTask implements Serializable {


    /*用来发件*/
    @Autowired
    private JavaMailSender javaMailSender;


    /*发件人的邮箱*/
    @Value("${emos.email.system}")
    private String mailbox;

    /*异步*/
    @Async
    public void sendAsync(SimpleMailMessage message){
        message.setFrom(mailbox);
        //抄送人
        message.setCc(mailbox);
        javaMailSender.send(message);
    }
}