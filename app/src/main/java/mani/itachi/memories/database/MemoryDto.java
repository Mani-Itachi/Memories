package mani.itachi.memories.database;

import java.io.Serializable;

/**
 * Created by ManikantaInugurthi on 02-02-2017.
 */

//todo lombok here
public class MemoryDto implements Serializable {

    private int id;
    private String name;
    private String date;
    private String type;
    private String desc;
    private String imagePath = "-1";
    private String bitmapPath = null;

    public MemoryDto() {
    }

    public MemoryDto(int id, String name, String type, String date, String desc, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
        this.desc = desc;
        this.imagePath = imagePath;
    }

    public String getBitmapPath() {
        return bitmapPath;
    }

    public void setBitmapPath(String bitmapPath) {
        this.bitmapPath = bitmapPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
