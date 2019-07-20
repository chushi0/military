package online.starlex.model;

/**
 * name：111代表红方司令
 * position：前两位代表x轴，后两位代表y轴
 * status：代表棋子状态
 */
public class Piece {
    private int name;
    private int position;

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
