package tl.com.timemanager.item;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ItemKindOfAction extends RealmObject {
    @PrimaryKey
    private int id;

    private String title;
    private int idResImage;
    private int idResBackground;
    private int idResColor;

    public ItemKindOfAction() {

    }

    public ItemKindOfAction(int id, String title, int idResImage, int idResBackground, int idResColor) {
        this.id = id;
        this.title = title;
        this.idResImage = idResImage;
        this.idResBackground = idResBackground;
        this.idResColor = idResColor;
    }

    public int getIdResBackground() {
        return idResBackground;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdResImage() {
        return idResImage;
    }

    public void setIdResImage(int idResImage) {
        this.idResImage = idResImage;
    }

    public int getIdResColor() {
        return idResColor;
    }

    public void setIdResColor(int idResColor) {
        this.idResColor = idResColor;
    }

    public String getTitle() {
        return title;
    }
}
