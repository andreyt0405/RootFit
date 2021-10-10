package com.sportschule.rootfit;
public class TrainingPropertyCollector {
    private String Date;
    private String Max_Participant;
    private String Time;
    private String Current_Participant;
    private String Trainer_uid;
    private String Rating;
    private String Trainer_name;
    private String Expertise;
    private int Image;

    public void setExpertise(String expertise) {
        Expertise = expertise;
    }

    public String getExpertise() {
        return Expertise;
    }

    public String getTrainer_name() {
        return Trainer_name;
    }

    public void setTrainer_name(String trainer_name) {
        Trainer_name = trainer_name;
    }


    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }
    public TrainingPropertyCollector() {

    }

    public String getDate()
    {
        return Date;
    }
    public void seDate(String Date)
    {
        this.Date = Date;
    }
    public String getMax_Participant()
    {
        return Max_Participant;
    }
    public void setMax_Participant(String Max_Participant)
    {
        this.Max_Participant = Max_Participant;
    }
    public String getTime()
    {
        return Time;
    }
    public void setTime(String Time)
    {
        this.Time = Time;
    }
    public int getImage(String expert)
    {
        switch (expert){
            case "Crossfit":
                return R.drawable.crossfit;
            case "Yoga":
                return R.drawable.yoga;
            case "Aerobics":
                return R.drawable.aerobics;
            case "Fitness":
                return R.drawable.fitness;
            default:
                return R.drawable.fitness;
        }
    }
    public String getCurrent_Participant()
    {
        return Current_Participant;
    }
    public void setCurrent_Participant(String Current_Participant)
    {
        this.Current_Participant = Current_Participant;
    }
    public String getTrainer_uid()
    {
        return Trainer_uid;
    }
    public void setTrainer_uid(String Trainer_uid)
    {
        this.Trainer_uid = Trainer_uid;
    }
}
