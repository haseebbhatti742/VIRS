package virs.app;

public class Person {
    private String cnic;
    private String name;
    private String phoneNumber;
    private String city;

    public Person(String cnic, String name, String phoneNumber, String city) {
        this.cnic = cnic;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.city = city;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
