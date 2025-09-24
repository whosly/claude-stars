package com.whosly.stars.springboot2.webflux.entry;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="t_user")
public class UserEntry {

    @Id    //主键id
    @GeneratedValue(strategy= GenerationType.AUTO)//主键生成策略
    private Long id;

    @Column(name="user_name")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="last_login_time")
    private Date lastLoginTime;

    private Integer sex;

}
