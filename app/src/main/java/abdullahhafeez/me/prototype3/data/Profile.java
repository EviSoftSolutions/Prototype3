package abdullahhafeez.me.prototype3.data;

/**
 * Created by Abdullah on 11/23/2017.
 */

public class Profile {

    private String name;
    private String email;
    private String profilePhotoUrl;

    public Profile(){}

    public Profile(String name, String email,  String profilePhotoUrl) {
        this.name = name;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
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


    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

}
