package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;

import jakarta.persistence.*;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.NeedURL;

import java.util.List;

@Entity
public class Lobby implements NeedURL {

    @Id
    @SequenceGenerator(name="sekwencja2", sequenceName = "lobby_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja2")
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String url;

    @Column(unique = true)
    private String code;

    private boolean isPrivate;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player host;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="lobbies_players",
            joinColumns = @JoinColumn(name="lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    private List<Player> players;

    public Lobby(Long id, String url, String code, boolean isPrivate, Player host, List<Player> players) {
        this.id = id;
        this.url = url;
        this.code = code;
        this.isPrivate = isPrivate;
        this.host = host;
        this.players = players;
    }

    public Lobby(String url, String code, boolean isPrivate, Player host, List<Player> players) {
        this.url = url;
        this.code = code;
        this.isPrivate = isPrivate;
        this.host = host;
        this.players = players;
    }

    public Lobby() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Player getHost() {
        return host;
    }

    public void setHost(Player host) {
        this.host = host;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }
}
