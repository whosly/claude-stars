package com.whosly.stars.springboot2.webflux.entry;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name="tbl")
@Proxy(lazy = false)
public class TblEntry  {
    @Id    //主键id
    @GeneratedValue(strategy= GenerationType.AUTO)//主键生成策略
    private Long id;

    private String title;

    @Column(name="author")
    private String author;

    @Column(name="create_date")
    private Date createDate;
}