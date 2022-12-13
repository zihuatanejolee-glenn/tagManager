package com.jun.li.res;

import java.util.List;

public class CodeRes {
    List<String> lists;
    int this_nums;

    public CodeRes(List<String> lists, int this_nums) {
        this.lists = lists;
        this.this_nums = this_nums;
    }

    public CodeRes() {
    }

    public List<String> getLists() {
        return lists;
    }

    public void setLists(List<String> lists) {
        this.lists = lists;
    }

    public int getThis_nums() {
        return this_nums;
    }

    public void setThis_nums(int this_nums) {
        this.this_nums = this_nums;
    }
}
