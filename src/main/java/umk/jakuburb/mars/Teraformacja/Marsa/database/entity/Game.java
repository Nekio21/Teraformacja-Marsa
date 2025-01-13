package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;

import jakarta.persistence.*;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.NeedURL;

import java.util.List;

@Entity
public class Game implements NeedURL {

    @Id
    @SequenceGenerator(name="sekwencja3", sequenceName = "game_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja3")
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String url;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name="game_players",
            joinColumns = @JoinColumn(name="game_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    private List<Player> players;

    private int countRounds;

    public Game() {
    }

    public Game(Long id, String url, List<Player> players, int countRounds) {
        this.id = id;
        this.url = url;
        this.players = players;
        this.countRounds = countRounds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public int getCountRounds() {
        return countRounds;
    }

    public void setCountRounds(int countRounds) {
        this.countRounds = countRounds;
    }
}
