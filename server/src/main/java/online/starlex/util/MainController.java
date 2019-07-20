package online.starlex.util;

import online.starlex.model.Piece;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class MainController {

    private Piece[] pieces = new Piece[50];
    private SocketHelper socketHelper = new SocketHelper();
    private boolean player1 = false;
    private boolean player2 = false;
    private Random random = new Random();

    public void logic() throws IOException {
        socketHelper.open();
        socketHelper.join();

        ThreadReceiver threadReceiver1 = new ThreadReceiver();
        threadReceiver1.setSocket(socketHelper.socketPool.get("Player1"));
        threadReceiver1.setTeam(1);
        Thread receive1 = new Thread(threadReceiver1);
        receive1.start();

        ThreadReceiver threadReceiver2 = new ThreadReceiver();
        threadReceiver2.setSocket(socketHelper.socketPool.get("Player2"));
        threadReceiver2.setTeam(2);
        Thread receive2 = new Thread(threadReceiver2);
        receive2.start();

    }

    private void gameLogic(int team, SocketHelper socketHelper, byte[] buffer) throws IOException {
        if (buffer[0] == 0) {
            byte[] queue = new byte[25];
            System.arraycopy(buffer, 1, queue, 0, buffer.length - 1);
            pieces = ready(team, queue);
            byte[] message = new byte[4];
            message[0] = 0;
            if (team == 1) {
                send(socketHelper.socketPool.get("Player2"), message);
                player1 = true;
            }
            if (team == 2) {
                send(socketHelper.socketPool.get("Player1"), message);
                player2 = true;
            }
            if (player1 && player2) {
                message[0] = 1;
                message[1] = 1;
                message[2] = (byte) random.nextInt(2);
                send(socketHelper.socketPool.get("Player1"), message);
                message[1] = 2;
                message[2] = (byte) (1 - message[2]);
                send(socketHelper.socketPool.get("Player2"), message);
            }
        } else {
            byte[] info = new byte[2];
            System.arraycopy(buffer, 1, info, 0, buffer.length - 1);
            isStepEnd(team, socketHelper, info);
        }
    }

    //传入队伍和25个棋子队列，得到一个Piece数组
    private Piece[] ready(int team, byte[] queue) {
        int jump = 0;
        for (int i = 0; i < 25; i++) {
            Piece piece = new Piece();
            piece.setName(team * 100 + queue[i]);
            if (i + jump == 6 || i + jump == 8 || i + jump == 12 || i + jump == 16 || i + jump == 18) {
                jump++;
            }
            if (team == 1) {
                piece.setPosition(i + jump);
                pieces[i] = piece;
            } else {
                piece.setPosition(59 - i - jump);
                pieces[i + 25] = piece;
            }
        }
        return pieces;
    }

    private void isStepEnd(int team, SocketHelper socketHelper, byte[] info) throws IOException {
        int position1;
        int position2;
        if (team == 1) {
            position1 = info[0];
            position2 = info[1];
        } else {
            position1 = 59 - info[0];
            position2 = 59 - info[1];
        }

        Piece piece1 = findByPosition(position1);
        Piece piece2 = findByPosition(position2);
        int name1 = piece1.getName() % 100;

        //回传信息大小
        byte[] message = new byte[4];
        byte[] extraMessage = new byte[4];
        byte[] gameMessage = new byte[4];
        message[0] = 2;
        if (piece2 != null) {
            //如果位置2有棋子
            int name2 = piece2.getName() % 100;
            if (name1 == 2 || name2 == 2) {
                //有一个炸弹
                deleteByPosition(position1);
                deleteByPosition(position2);
                message[1] = 3;
                extraMessage[0] = 3;
                if (name1 == 11) {
                    //棋子1是司令，棋子2是炸弹
                    extraMessage[1] = nameToPosition(100);
                    send(socketHelper.socketPool.get("Player2"), extraMessage);
                    isGameEnd();
                } else if (name2 == 11) {
                    //棋子1是炸弹，棋子2是司令
                    extraMessage[1] = nameToPosition(200);
                    send(socketHelper.socketPool.get("Player1"), extraMessage);
                    isGameEnd();
                }
            } else if (name2 == 1) {
                //棋子2是地雷
                if (name1 == 3) {
                    deleteByPosition(position2);
                    move(position1, position2);
                    message[1] = 1;
                    isGameEnd();
                } else if (name1 == 11) {
                    deleteByPosition(position1);
                    deleteByPosition(position2);
                    message[1] = 3;
                    extraMessage[0] = 3;
                    extraMessage[1] = nameToPosition(100);
                    send(socketHelper.socketPool.get("Player2"), extraMessage);
                    isGameEnd();
                } else {
                    deleteByPosition(position1);
                    deleteByPosition(position2);
                    message[1] = 3;
                    isGameEnd();
                }
            } else if (name2 == 0) {
                //棋子2是军旗
                deleteByPosition(position2);
                move(position1, position2);
                gameMessage[0] = 4;
                gameMessage[1] = 1;
                gameMessage[2] = 0;
                send(socketHelper.socketPool.get("Player1"), gameMessage);
                send(socketHelper.socketPool.get("Player2"), gameMessage);
            } else if (name1 == 11 && name2 == 11) {
                //棋子1和2都是司令
                deleteByPosition(position1);
                deleteByPosition(position2);
                message[1] = 3;
                extraMessage[0] = 3;
                extraMessage[1] = nameToPosition(100);
                send(socketHelper.socketPool.get("Player2"), extraMessage);
                extraMessage[0] = 3;
                extraMessage[1] = nameToPosition(200);
                send(socketHelper.socketPool.get("Player1"), extraMessage);
                isGameEnd();
            } else {
                if (name1 > name2) {
                    move(position1, position2);
                    deleteByPosition(position2);
                    message[1] = 1;
                    isGameEnd();
                } else if (name1 < name2) {
                    deleteByPosition(position1);
                    message[1] = 2;
                    isGameEnd();
                } else {
                    assert name1 == name2;
                    deleteByPosition(position1);
                    deleteByPosition(position2);
                    message[1] = 3;
                    isGameEnd();
                }
            }
        } else {
            //如果位置2没有棋子
            move(position1, position2);
            message[1] = 0;
        }
        message[2] = (byte) position1;
        message[3] = (byte) position2;
        send(socketHelper.socketPool.get("Player1"), message);
        message[2] = (byte) (59 - position1);
        message[3] = (byte) (59 - position2);
        send(socketHelper.socketPool.get("Player2"), message);

    }

    private void isGameEnd() throws IOException {
        byte[] gameMessage = new byte[4];
        gameMessage[0] = 4;
        gameMessage[2] = 1;
        boolean winner1 = false;
        boolean winner2 = false;
        Piece[] newPieces = pieces.clone();
        for (int i = 0; i < newPieces.length; i++) {
            int position = newPieces[i].getPosition();
            int type = newPieces[i].getName() % 100;
            if (position == 25) {
                if (checkPiece(25, 24) && checkPiece(25, 26)) {
                    newPieces[i] = null;
                }
            }
            if (position == 27) {
                if (checkPiece(27, 22) && checkPiece(27, 26) && checkPiece(27, 28)) {
                    newPieces[i] = null;
                }
            }
            if (position == 29) {
                if (checkPiece(29, 24) && checkPiece(29, 28)) {
                    newPieces[i] = null;
                }
            }
            if (position == 30) {
                if (checkPiece(30, 35) && checkPiece(30, 31)) {
                    newPieces[i] = null;
                }
            }
            if (position == 32) {
                if (checkPiece(32, 37) && checkPiece(32, 31) && checkPiece(32, 33)) {
                    newPieces[i] = null;
                }
            }
            if (position == 34) {
                if (checkPiece(34, 39) && checkPiece(34, 33)) {
                    newPieces[i] = null;
                }
            }
            if (position == 26 || position == 28 || position == 31 || position == 33) {
                newPieces[i] = null;
            }
            if (type == 0 || type == 1 || type == 2) {
                newPieces[i] = null;
            }
        }
        for (Piece piece : newPieces) {
            if (piece == null) {
                continue;
            } else {
                if (piece.getName() / 100 == 1) {
                    winner1 = true;
                }
                if (piece.getName() / 100 == 2) {
                    winner2 = true;
                }
            }
        }
        if (winner1 && winner2) {
        } else if (winner1) {
            gameMessage[1] = 1;
            send(socketHelper.socketPool.get("Player1"), gameMessage);
            send(socketHelper.socketPool.get("Player2"), gameMessage);
        } else if (winner2) {
            gameMessage[1] = 2;
            send(socketHelper.socketPool.get("Player1"), gameMessage);
            send(socketHelper.socketPool.get("Player2"), gameMessage);
        }
    }

    private void send(Socket socket, byte[] message) throws IOException {
        OutputStream writer = socket.getOutputStream();
        writer.write(message);
        writer.flush();
    }

    private Piece findByPosition(int position) {
        for (Piece piece : pieces) {
            if (piece.getPosition() == position) {
                return piece;
            }
        }
        return null;
    }

    private byte nameToPosition(int name) {
        for (Piece piece : pieces) {
            if (piece.getName() == name) {
                return (byte) piece.getPosition();
            }
        }
        return -1;
    }

    private void deleteByPosition(int position) {
        for (Piece piece : pieces) {
            if (piece.getPosition() == position) {
                piece.setName(-1);
                piece.setPosition(-1);
            }
        }
    }

    private void move(int position1, int position2) {
        Piece piece = findByPosition(position1);
        piece.setPosition(position2);
    }

    private boolean checkPiece(int position1, int position2) {
        Piece piece1 = findByPosition(position1);
        Piece piece2 = findByPosition(position2);
        if (piece2 != null) {
            return piece2.getName() / 100 != piece1.getName() / 100;
        }
        return true;
    }

    public class ThreadReceiver implements Runnable {

        private java.net.Socket socket;
        private int team;

        @Override
        public void run() {
            DataInputStream reader;
            try {
                reader = new DataInputStream(socket.getInputStream());
                int info;
                //noinspection InfiniteLoopStatement
                while (true) {
                    info = reader.readInt();
                    if (info == 0) {
                        continue;
                    }
                    byte[] buffer = new byte[info];
                    reader.readFully(buffer);
                    gameLogic(team, socketHelper, buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setSocket(java.net.Socket socket) {
            this.socket = socket;
        }

        public void setTeam(int team) {
            this.team = team;
        }
    }

}

