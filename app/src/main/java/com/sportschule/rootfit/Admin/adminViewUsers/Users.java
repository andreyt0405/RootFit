package com.sportschule.rootfit.Admin.adminViewUsers;

public class Users {
    String Age = null;
    String Email = null;
    String First = null;
    String Last = null;
    String Phone = null;
    String State = null;
    String Username = null;
    /*private String id = null;*/
    String Gender = null;
    String Expert = null;

    public Users(){}

    public Users(String age, String email, String first, String last, String phone, String state, String username, String gender) {/*, String id*/
        this.Age = age;
        this.Email = email;
        this.First = first;
        this.Last = last;
        this.Phone = phone;
        this.State = state;
        this.Username = username;
        /*this.id = id;*/
        this.Gender = gender;
    }

    /*public users(String ID){this.id = ID;}*/


    public String getAge() {
        return this.Age;
    }

    public String getEmail() {
        return this.Email;
    }

    public String getFirst() {
        return this.First;
    }

    public String getLast() {
        return this.Last;
    }

    public String getPhone() {
        return this.Phone;
    }

    public String getState() {
        return this.State;
    }

    public String getUsername() {
        return this.Username;
    }

    /*public String getId() {
        return this.id;
    }*/

    public String getGender() { return Gender; }

    public String getExpert() {
        return Expert;
    }

    public void setExpert(String expert) {
        Expert = expert;
    }

    public void setAge(String age) {
        this.Age = age;
    }

    public String createAccountInfoString(){
        return  "Age:       " + this.Age + "\n\r" +
                "Email:     " + this.Email + "\n\r" +
                "First:     " + this.First + "\n\r" +
                "Last:      " + this.Last + "\n\r" +
                "Phone:     " + this.Phone + "\n\r" +
                "State:     " + this.State + "\n\r" +
                "Username:  " + this.Username + "\n\r" +
                "Gender:        " + this.Gender + "\n\r";


    }
}
