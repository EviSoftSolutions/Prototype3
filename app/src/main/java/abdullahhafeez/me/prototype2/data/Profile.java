package abdullahhafeez.me.prototype2.data;

/**
 * Created by Abdullah on 11/23/2017.
 */

public class Profile {

    private String name;
    private String email;

    public Profile(){}

    public Profile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
