package umk.jakuburb.mars.Teraformacja.Marsa.message;

import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static List<Area> getBoardTF(Area newArea, List<Area> board) throws Exception {
        List<Area> boardToSend = new ArrayList<>();

        switch (newArea){
            case OCEAN -> {
                getBoardTFOcean(boardToSend, board);
            }
            case TREE_P1 -> {
                getBoardTFTree(boardToSend, board, 0);
            }
            case TREE_P2 -> {
                getBoardTFTree(boardToSend, board, 1);
            }
            case TREE_P3 -> {
                getBoardTFTree(boardToSend, board, 2);
            }
            case CITY_P1, CITY_P2, CITY_P3 -> {
                getBoardTFCity(boardToSend, board);
            }
        }

        return boardToSend;
    }

    private static void getBoardTFOcean(List<Area> newBoard, List<Area> board){
        for(Area a: board){
            if(a==Area.NO_OCEAN)newBoard.add(Area.TRUE);
            else newBoard.add(Area.FALSE);
        }
    }

    private static void getBoardTFCity(List<Area> newBoard, List<Area> board){
        for(int i=0;i<board.size();i++){
            if(board.get(i) == Area.NOTHING){
                newBoard.set(i, Area.TRUE);
            }else{
                newBoard.set(i, Area.FALSE);
            }
        }

        for(int i=0;i<board.size();i++){
            if(board.get(i) == Area.CITY_P1 || board.get(i) == Area.CITY_P2 || board.get(i) == Area.CITY_P3){
                List<Integer> list = areasAround(i);
                for(int k=0;k<list.size();k++){
                    newBoard.set(list.get(k), Area.FALSE);
                }
            }
        }
    }

    private static void getBoardTFTree(List<Area> newBoard, List<Area> board, int indexPlayer) throws Exception {
        boolean is = false;
        Area city;
        Area tree;

        switch (indexPlayer){
            case 0 ->{
                city = Area.CITY_P1;
                tree = Area.TREE_P1;
            }
            case 1 ->{
                city = Area.CITY_P2;
                tree = Area.TREE_P2;
            }
            case 2 ->{
                city = Area.CITY_P3;
                tree = Area.TREE_P3;
            }
            default ->{
                throw new Exception("Index not exist");
            }
        }

        for(Area a: board){
            newBoard.add(Area.FALSE);
            if(a == tree || a == city){
                is = true;
            }
        }

        if(is){
            for(int i=0;i<board.size(); i++){
                if(board.get(i) == tree || board.get(i) == city){
                    List<Integer> list = areasAround(i);
                    for(int k=0;k<list.size();k++){
                        if(board.get(list.get(k)) == Area.NOTHING){
                            newBoard.set(list.get(k), Area.TRUE);
                        }
                    }
                }
            }
        }else{
            for(int i=0;i<board.size();i++){
                if(board.get(i) == Area.NOTHING){
                    newBoard.set(i, Area.TRUE);
                }
            }
        }

        newBoard.forEach(System.out::print);
    }

    //Only for default boards
    private static List<Integer> areasAround(int index){
        List<Integer> around = new ArrayList<>();
        int levelLength[] = {5,6,7,8,9,8,7,6,5};

        int level = checkLevel(index);

        int left = index-1;
        int right = index+1;

        if(checkLevel(left) == level){
            around.add(left);
        }

        if(checkLevel(right) == level){
            around.add(right);
        }

        if(level > 1) {
            int up1 = index - levelLength[level-1];
            int up2 = index - levelLength[level-2];

            if(checkLevel(up1) == level-1){
                around.add(up1);
            }

            if(checkLevel(up2) == level-1){
                around.add(up2);
            }
        }

        if(level < 9) {
            int down1 = index + levelLength[level-1];
            int down2 = index + levelLength[level];

            if(checkLevel(down1) == level+1){
                around.add(down1);
            }
            if(checkLevel(down2) == level+1){
                around.add(down2);
            }
        }

        return around;
    }

    private static  int checkLevel(int index){
        if(index >=0 && index <= 4){
            return 1;
        }
        else if(index >= 5 && index <= 10){
            return 2;
        }
        else if(index >= 11 && index <= 17){
            return 3;
        }
        else if(index >= 18 && index <= 25){
            return 4;
        }
        else if(index >= 26 && index <= 34){
            return 5;
        }
        else if(index >= 35 && index <= 42){
            return 6;
        }
        else if(index >= 43 && index <= 49){
            return 7;
        }
        else if(index >= 50 && index <= 55){
            return 8;
        }
        else if(index >= 56 && index <= 60){
            return 9;
        }

        return -1;
    }
}
