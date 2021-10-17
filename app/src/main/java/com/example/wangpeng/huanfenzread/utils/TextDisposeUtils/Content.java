package com.example.wangpeng.huanfenzread.utils.TextDisposeUtils;

public class Content {
    private long id;
    private String name;
    //txt所带章节
    private String chapter;
    private String content;
    //新生成章节
    private long number;

    public Content() {
    }

    public Content(long id, String name, String chapter, String content, long number) {
        this.id = id;
        this.name = name;
        this.chapter = chapter;
        this.content = content;
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", chapter='" + chapter + '\'' +
                ", content='" + content + '\'' +
                ", number=" + number +
                '}';
    }
}