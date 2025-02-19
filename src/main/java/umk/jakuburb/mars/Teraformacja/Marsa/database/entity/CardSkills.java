package umk.jakuburb.mars.Teraformacja.Marsa.database.entity;

import jakarta.persistence.*;

@Entity
public class CardSkills {

    public enum Move{
        TAKE, GET, LOOSE
    }

    public enum Resource{
        GOLD_PROD, GOLD,
        ENERGY_PROD, ENERGY,
        HEAT_PROD, HEAT,
        METAL_PROD, METAL,
        PLANTS_PROD, PLANTS,
        TITANIUM_PROD, TITANIUM,
        OCEAN,
        TEMP,
        OXYGEN,
        CARD,
        PZ,
        CITY,
        TREE
    }
    @Id
    @SequenceGenerator(name="sekwencja6", sequenceName = "card_skills_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sekwencja6")
    @Column(unique = true)
    private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="card_id")
    private Card card;

    private Move move;

    private Resource resource;
    private int amount;

    private Resource whenUse;

    public CardSkills() {
    }

    public CardSkills(Move move, Resource resource, int amount) {
        this.move = move;
        this.resource = resource;
        this.amount = amount;
    }

    public CardSkills(Long id, Card card, Move move, Resource resource, int amount) {
        this.id = id;
        this.card = card;
        this.move = move;
        this.resource = resource;
        this.amount = amount;
    }


    public CardSkills(Card card, Move move, Resource resource, int amount) {
        this.card = card;
        this.move = move;
        this.resource = resource;
        this.amount = amount;
    }

    public CardSkills(Long id, Card card, Move move, Resource resource, int amount, Resource whenUse) {
        this.id = id;
        this.card = card;
        this.move = move;
        this.resource = resource;
        this.amount = amount;
        this.whenUse = whenUse;
    }

    public CardSkills(Move move, Resource resource, int amount, Resource whenUse) {
        this.move = move;
        this.resource = resource;
        this.amount = amount;
        this.whenUse = whenUse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Resource getWhenUse() {
        return whenUse;
    }

    public void setWhenUse(Resource whenUSe) {
        this.whenUse = whenUse;
    }
}
