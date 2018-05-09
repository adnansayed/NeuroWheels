package onestopsolns.neurowheels.model;

/**
 * Created by Adnan on 17-04-2018.
 */

public class MedicineModel {
    int id;
    String name;

    public MedicineModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
