package tech.claudioed.users.data;

public class CreatedUser {

  private String id;
  private String firstName;
  private String lastName;
  private String email;

  CreatedUser() {
  }

  private CreatedUser(String id, String firstName, String lastName, String email) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public static CreatedUser createNew(String id, String firstName, String lastName, String email) {
    return new CreatedUser(id, firstName, lastName, email);
  }

  public User toData(){
    return User.createUser(this.id,this.firstName,this.lastName,this.email,Boolean.FALSE,Boolean.FALSE);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
