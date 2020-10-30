package mani.itachi.memories.database;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ManikantaInugurthi on 02-02-2017.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryDto implements Serializable {

    private int id;
    private String name;
    private String date;
    private String type;
    private String desc;
    private String imagePath = "-1";
    private String bitmapPath = null;

    public MemoryDto(int id, String name, String type, String date, String desc, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.date = date;
        this.desc = desc;
        this.imagePath = imagePath;
    }

}
