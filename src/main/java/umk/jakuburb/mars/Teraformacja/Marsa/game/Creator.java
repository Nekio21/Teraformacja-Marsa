package umk.jakuburb.mars.Teraformacja.Marsa.game;

import org.springframework.data.jpa.repository.JpaRepository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Lobby;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.Player;
import umk.jakuburb.mars.Teraformacja.Marsa.exception.CodeSpaceException;
import umk.jakuburb.mars.Teraformacja.Marsa.utils.NeedURL;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class Creator {

    protected char[] letter = new char[62];
    protected Random random;


    private List<String> allURL;

    public Creator(){
        random = new Random();
        initLetter();
    }

    public abstract String create(Player player) throws Exception;

    public abstract void createExchange(String name);


    protected String setURL(NeedURL nurl) throws Exception{
        String URL = findURL();

        if(URL == null){
            throw new CodeSpaceException();
        }

        nurl.setUrl(URL);

        save(nurl);

        return URL;
    }

    //
    protected abstract void save(NeedURL nurl);
    protected abstract List<String> getAllURL();

    private String findURL(){
        for(int i=0;i<3;i++) {
            String url = UUID.randomUUID().toString();
            boolean check = checkURL(url);

            if (check) {
                return url;
            }
        }

        return null;
    }

    private boolean checkURL(String URL){
        allURL = getAllURL();

        return allURL.stream().noneMatch(URL::equals);
    }

    private void initLetter(){
        for(int i=0; i<10; i++){
            letter[i] = (char)(48+i);
        }

        for(int i=10;i<36;i++){
            letter[i] = (char)(i+55);
        }

        for(int i=36;i<62;i++){
            letter[i] = (char)(i+61);
        }
    }
}
