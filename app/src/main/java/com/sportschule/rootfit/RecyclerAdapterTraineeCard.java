package com.sportschule.rootfit;
public class RecyclerAdapterTraineeCard {
    private String Age;
    private String Last;
    private String Name;
    private String Uid;
    RecyclerAdapterTraineeCard()
    {

    }
    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public String getUid() {
        return Uid;
    }

    public String getAge() {
        return Age;
    }

    public String getLast() {
        return Last;
    }

    public String getName() {
        return Name;
    }
    public void setAge(String Age) {
        this.Age = Age;
    }

    public void setLast(String Last) {
        this.Last = Last;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
}

