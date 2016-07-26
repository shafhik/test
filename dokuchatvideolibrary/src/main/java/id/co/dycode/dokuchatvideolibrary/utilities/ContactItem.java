package id.co.dycode.dokuchatvideolibrary.utilities;

/**
 * Created by 1 on 7/5/2016.
 */
public class ContactItem {
    private String name = "";
    private String refId = "";
    private String avatar = "";

    //Selectable
    private boolean isSelected = false;

    public ContactItem() {
    }

    public ContactItem(String name, String refId, String avatar) {
        this.name = name;
        this.refId = refId;
        this.avatar = avatar;
    }


    public void SetRefId(String refId) {
        this.refId = refId;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String GetName() {
        return name;
    }

    public String GetRefiId() {
        return refId;
    }

    public String GetAvatar() {
        return avatar;
    }
}
