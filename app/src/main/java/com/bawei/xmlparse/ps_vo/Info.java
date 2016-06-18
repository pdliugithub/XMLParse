package com.bawei.xmlparse.ps_vo;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/3.
 */
public class Info implements Serializable {

    private int id;
    private String catalog;


    public Info() {
        super();
    }

    @Override
    public String toString() {
        return "Info{" +
                "id=" + id +
                ", catalog='" + catalog + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
}
