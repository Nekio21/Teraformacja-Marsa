package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Player {

    public enum Role{USER, ADMIN, LEMONIADA}

    @Id
    @SequenceGenerator(name="sekwencja", sequenceName = "player_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja")
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String login;

    private String passwd;

    private List<Role> role;

    @ManyToMany(mappedBy = "players", fetch=FetchType.LAZY)
    private List<Lobby> lobbies;


    public Player(Long id, String login, String passwd, List<Role> role, List<Lobby> lobbies) {
        this.id = id;
        this.login = login;
        this.passwd = passwd;
        this.role = role;
        this.lobbies = lobbies;
    }

    public Player(String login, String passwd, List<Role> role) {
        this.login = login;
        this.passwd = passwd;
        this.role = role;
    }

    public Player(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public List<Role> getRole() {
        return role;
    }

    public void setRole(List<Role> role) {
        this.role = role;
    }

    public List<Lobby> getLobbies() {
        return lobbies;
    }

    public void setLobbies(List<Lobby> lobbies) {
        this.lobbies = lobbies;
    }
}
