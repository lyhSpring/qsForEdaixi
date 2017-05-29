package bjtu.model;

/**
 * Created by 李奕杭_lyh on 2017/5/16.
 */

public class Courier {
    private int id;
    private String name;
    private int status;
    private String email;
    private String mobile;
    private String password;
    private int station_id;
    private String role = "qs";

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPassword() {
        return password;
    }

    public int getStation_id() {
        return station_id;
    }

    public String getRole() {
        return role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStation_id(int station_id) {
        this.station_id = station_id;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
