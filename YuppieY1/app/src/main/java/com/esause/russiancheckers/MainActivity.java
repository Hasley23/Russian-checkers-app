package com.esause.russiancheckers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    // App context
    private Context context;

    // Array block
    private ImageView[][] ivCell = new ImageView[Board.BOARD_SIZE][Board.BOARD_SIZE];   // board
    private Drawable[] drawCell = new Drawable[10];   // board states

    // TextView block
    private TextView tvTurn;
    private TextView tvWhiteScore;
    private TextView tvBlackScore;

    // Miscellaneous block
    private boolean isClicked = false;//tracking player's click
    private boolean isMarked = false;//tracking marked cells

    // *** GAME BLOCK ***
    private Game Core;   // Checkers core
    private boolean isGameRunning = false;
    private int xMove, yMove, xMove2, yMove2;   // path coordinates
    private boolean whiteOnTop = false;   // are white pieces on top of the board?
    private boolean possibleSecondMove = false;
    private boolean playWithAI = false;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Use this to prepare the game */

        // main layout
        setContentView(R.layout.content_main);

        // set app context
        context=this;

        // expand layout to status bar
        findViewById(R.id.content_main).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Background animation
        AnimationDrawable animationDrawable = (AnimationDrawable) findViewById(
                R.id.content_main).getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        // Prepare game
        loadResources();
        setListen();
        startBoardGame();
    }

    @SuppressLint("NewApi")
    private void setListen() {
        // Button block
        Button btnNewGame = findViewById(R.id.btnPlay);

        // TextView block
        tvTurn = findViewById(R.id.tvTurn);
        tvBlackScore = findViewById(R.id.tvBlackScore);
        tvWhiteScore = findViewById(R.id.tvWhiteScore);

        // Setters
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show dialog to choose game params
                showRadioButtonDialog();
            }
        });
    }

    /**
     * Shows new game dialog
     */
    @SuppressLint("NewApi")
    private void showRadioButtonDialog() {
        /* Use this to get params and start the game */
        // declare dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.radiobutton_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // declare RadioGroup
        RadioGroup rg = dialog.findViewById(R.id.radio_group);

        // access to dialog content view
        View lt = dialog.getWindow().getDecorView();

        // Button block
        Button btnPlayerVsPlayer = lt.findViewById(R.id.btnPlayWithoutAI);
        Button btnPlayerVsAI = lt.findViewById(R.id.btnPlayWithAI);

        // check necessary radio button
        if (!whiteOnTop) {
            rg.check(R.id.radio0);
        }
        else {
            rg.check(R.id.radio1);
        }

        // Setters
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                whiteOnTop = group.getCheckedRadioButtonId() == R.id.radio1;
            }
        });

        btnPlayerVsPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGameRunning = true;
                playWithAI = false;
                init_game();
                dialog.dismiss();
            }
        });

        btnPlayerVsAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGameRunning = true;
                playWithAI = true;
                init_game();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Init game with chosen params
     */
    private void init_game() {
        // Init vars

        isClicked = false;//tracking player's click
        isMarked = false;//tracking marked cells

        xMove = 0;
        yMove = 0;
        xMove2 = 0;
        yMove2 = 0;

        possibleSecondMove = false;

        // Init core
        Core = new Game(0,0, Game.Player.BLACK,
                false, whiteOnTop, playWithAI);

        // Output
        updateGraphics();
        tvTurn.setText(Core.GetCurrentPlayer(context));
    }

    /**
     * Updates board and score
     */
    @SuppressLint({"SetTextI18n", "NewApi"})
    private void updateGraphics() {
        /* Use this to draw a board */
        // get states array
        Cell[][] cellsState = Core.getMBoard().getCellsState();

        // parse states array and draw pieces
        for(int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (cellsState[i][j].GetState() == Cell.State.WHITE
                        && cellsState[i][j].GetIsQueen()){
                    ivCell[i][j].setImageDrawable(drawCell[5]);
                }
                else if (cellsState[i][j].GetState() == Cell.State.BLACK
                        && cellsState[i][j].GetIsQueen()){
                    ivCell[i][j].setImageDrawable(drawCell[6]);
                }
                else if (cellsState[i][j].GetState() == Cell.State.WHITE) {
                    ivCell[i][j].setImageDrawable(drawCell[2]);
                }
                else if (cellsState[i][j].GetState() == Cell.State.BLACK) {
                    ivCell[i][j].setImageDrawable(drawCell[3]);
                }
                else {
                    ivCell[i][j].setImageDrawable(null);
                }
                if (ivCell[i][j].getBackground() == drawCell[7]
                        || ivCell[i][j].getBackground() == drawCell[8]){
                    ivCell[i][j].setBackground(drawCell[0]);
                }
            }
        }

        // draw score
        tvBlackScore.setText(Integer.toString(Core.getIBlackScore()));
        tvWhiteScore.setText(Integer.toString(Core.getIWhiteScore()));
    }


    /**
     * Load board images
     */
    private void loadResources() {
        /* Use this to load cell drawables */
        Drawable redInset = context.getResources().getDrawable(R.drawable.white);
        redInset.setLevel(1);

        //cell with states
        drawCell[0] = context.getResources().getDrawable(R.drawable.white);
        //cell without states
        drawCell[1] = context.getResources().getDrawable(R.drawable.blue_square);
        //white piece
        drawCell[2] = context.getResources().getDrawable(R.drawable.white_piece);
        //black piece
        drawCell[3] = context.getResources().getDrawable(R.drawable.black_piece);
        //red inset
        drawCell[4] = redInset;
        //white queen
        drawCell[5] = context.getResources().getDrawable(R.drawable.white_queen);
        //black queen
        drawCell[6] = context.getResources().getDrawable(R.drawable.black_queen);
        //pink inset
        drawCell[7] = context.getResources().getDrawable(R.drawable.white_pink_border);
        //yellow inset
        drawCell[8] = context.getResources().getDrawable(R.drawable.white_yellow_border);
        //fix
        drawCell[9] = context.getResources().getDrawable(R.drawable.transparent_piece);
        Log.d("Main_class", "Resources loaded!");
    }

    /**
     * Main loop
     */
    @SuppressLint("NewApi")
    private void startBoardGame() {
        /* Use this to start a game session */

        // layout works
        final int sizeOfCell = Math.round(boardViewWidth() / Board.BOARD_SIZE);
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(
                sizeOfCell * Board.BOARD_SIZE, sizeOfCell);
        final LinearLayout.LayoutParams lpCell = new LinearLayout.LayoutParams(
                sizeOfCell, sizeOfCell);
        final LinearLayout linBoard = findViewById(R.id.linBoard);

        // Main loop
        for(int i = 0; i < Board.BOARD_SIZE; i++) {
            LinearLayout linRow = new LinearLayout(context);
            //make a row
            for(int j=0; j < Board.BOARD_SIZE; j++) {
                ivCell[i][j] = new ImageView(context);
                // Fill
                ivCell[i][j].setBackground(drawCell[1]);
                final int x = j;
                final int y = i;
                // main listener
                ivCell[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // !isGameRunning --> board untouchable
                        if (isGameRunning) {
                            // set white spaces untouchable
                            if ((x % 2 == 0 && y % 2 != 0) || (x % 2 != 0 && y % 2 == 0)) {
                                // first click
                                if (!isClicked) {
                                    // set BLANK spaces untouchable
                                    if (Core.getMBoard().getCellsState()[y][x].GetState()
                                            != Cell.State.BLANK)
                                    {
                                        isClicked = true;
                                        xMove = x;
                                        yMove = y;

                                        // make inset
                                        if (ivCell[yMove][xMove].getBackground() == drawCell[8]){
                                            isMarked = true;
                                        }
                                        ivCell[yMove][xMove].setBackground(drawCell[4]);
                                    }
                                }
                                else {
                                    // second click
                                    xMove2 = x;
                                    yMove2 = y;
                                    isClicked = false;

                                    // undo inset
                                    if (!isMarked){
                                        ivCell[yMove][xMove].setBackground(drawCell[0]);
                                    }
                                    else {
                                        ivCell[yMove][xMove].setBackground(drawCell[8]);
                                        isMarked = false;
                                    }

                                    // Call game core for a move
                                    boolean res = Core.MainAction(new vector(new position(
                                            yMove,xMove), new position(yMove2,xMove2)));

                                    // Multiple fight situation
                                    possibleSecondMove = Core.getPossibleSecondMove();

                                    // cut off failed moves
                                    if (res) {
                                        visualizeMoves(lpCell);
                                    }
                                }
                            }
                        }
                    }
                });

                if ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0))
                {
                    ivCell[i][j].setBackground(drawCell[0]);
                }

                linRow.addView(ivCell[i][j], lpCell);
            }
            linBoard.addView(linRow, lpRow);
        }
    }

    /**
     * Play animation and make AI move
     * @param lpCell board cell params
     */
    private void visualizeMoves(final LinearLayout.LayoutParams lpCell){
        // declare animation path
        final ArrayList<vector> anim = new ArrayList<>();
        // add player path
        anim.add(new vector(new position(yMove,xMove),
                new position(yMove2,xMove2)));
        // get animation
        final AnimatorSet set = translationAnim(anim, lpCell);

        // listen for the end of animation
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(
                    Animator animation) {
                findViewById(R.id.pieceAnim).
                        setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // hide animation view
                findViewById(R.id.pieceAnim).setVisibility(
                        View.GONE);
                // update board softly (animation fix)
                clearGhosts(anim);
                if (Core.GetWinner() == Game.Player.NONE) {
                    if (possibleSecondMove){
                        // multiple fight
                        xMove = xMove2;
                        yMove = yMove2;
                        updateMove();
                        isGameRunning = true;
                    } else {
                        if (playWithAI && Core.GetWinner()
                                == Game.Player.NONE){
                            clearInsets();
                            ArrayList<vector> aiMove = Core.aiMovement();

                            if (aiMove == null) {
                                finalDialog();
                            } else {
                                // get animation for AI
                                AnimatorSet set2 = translationAnim(
                                        aiMove, lpCell);
                                set2.addListener(
                                        new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationStart(
                                                    Animator animation) {
                                                findViewById(R.id.pieceAnim).
                                                        setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationEnd(
                                                    Animator animation) {
                                                findViewById(R.id.pieceAnim).
                                                        setVisibility(View.GONE);
                                                updateMove();
                                                isGameRunning = true;
                                            }
                                        });
                                set2.start();
                            }

                        } else {
                            updateMove();
                            isGameRunning = true;
                        }
                    }
                } else {
                    isGameRunning = false;
                    updateGraphics();
                    finalDialog();
                }
            }
        });
        set.start();
    }

    /**
     * Animation stuff
     * @param path move path
     * @param lpCell cell params
     * @return ready to play animation
     */
    @SuppressLint("NewApi")
    private AnimatorSet translationAnim(final ArrayList<vector> path,
                                        final LinearLayout.LayoutParams lpCell){
        /* Use this for translation animation */

        // lock the board
        isGameRunning = false;

        // find animation view
        LinearLayout animSpace = findViewById(R.id.animator);
        ImageView piece = findViewById(R.id.pieceAnim);

        final int[] location = new int[2];

        ivCell[path.get(0).getFirst().getY()][path.get(0).getFirst().getX()].
                getLocationOnScreen(location);

        if (path.size() != 0) {
            // prepare board and animation view
            piece.setImageDrawable(
                    ivCell[path.get(0).getFirst().getY()][path.get(0).getFirst().getX()].
                            getDrawable());
            animSpace.updateViewLayout(piece, lpCell);
            ivCell[path.get(0).getFirst().getY()][path.get(0).getFirst().getX()].
                    setImageDrawable(null);
        }

        // set animation path
        Path animPath = new Path();
        animPath.moveTo(location[0], location[1]);

        for (vector v : path) {
            ivCell[v.getSecond().getY()][v.getSecond().getX()].getLocationOnScreen(location);
            animPath.lineTo(location[0], location[1]);
        }

        //piece.setVisibility(View.VISIBLE);
        // add path to animator
        ValueAnimator pathAnimator = ObjectAnimator.ofFloat(
                piece, "x", "y", animPath);
        pathAnimator.setDuration(path.size() * 500);

        // add animator to animator set
        AnimatorSet set = new AnimatorSet();
        set.play(pathAnimator);
        return set;
    }

    /**
     * Update after animation
     */
    private void updateMove(){
        // Update board
        updateGraphics();
        // Clear board and check game ending
        clearAndCheck();
    }

    /**
     * Check for winner and make highlights
     */
    @SuppressLint("NewApi")
    private void clearAndCheck() {
        if (Core.GetWinner() == Game.Player.NONE){
            tvTurn.setText(Core.GetCurrentPlayer(context));
            if (Core.getFightExpected()) {
                ArrayList<vector> vectors = Core.getFightExpectedPositions();
                for (int i = 0; i < vectors.size(); i++) {
                    ivCell[vectors.get(i).getFirst().getY()][vectors.get(i).getFirst().getX()].
                            setBackground(drawCell[8]);
                    ivCell[vectors.get(i).getSecond().getY()][vectors.get(i).getSecond().getX()].
                            setBackground(drawCell[7]);
                }
            }
        }
        else {
            // Call ending
            isGameRunning = false;
            finalDialog();
        }
    }

    /**
     * Clear highlights
     */
    @SuppressLint("NewApi")
    private void clearInsets() {
        for(int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (ivCell[i][j].getBackground() == drawCell[7]
                        || ivCell[i][j].getBackground() == drawCell[8]){
                    ivCell[i][j].setBackground(drawCell[0]);
                }
            }
        }
    }

    /**
     * Clear murdered pieces after animation
     * @param path murderer way
     */
    private void clearGhosts(ArrayList<vector> path) {

        if (ivCell[yMove2][xMove2].getDrawable() != drawCell[5] &&
                ivCell[yMove2][xMove2].getDrawable() != drawCell[6]) {
            for (vector v : path) {
                int y = (v.getFirst().getY() + v.getSecond().getY()) / 2;
                int x = (v.getFirst().getX() + v.getSecond().getX()) / 2;

                if (y % 2 == 0 || x % 2 == 0) {
                    ivCell[y][x].setImageDrawable(null);
                }
            }
        }
        else {
            int i = path.get(0).getFirst().getY();
            int j = path.get(0).getFirst().getX();
            do {
                if (path.get(0).getSecond().getX() > path.get(0).getFirst().getX())
                    j++;
                else j--;
                if (path.get(0).getSecond().getY() > path.get(0).getFirst().getY())
                    i++;
                else i--;
                ivCell[i][j].setImageDrawable(null);
            } while (i != path.get(0).getSecond().getY() && j != path.get(0).getSecond().getX());
        }

        // update piece after animation
        ImageView drawable = findViewById(R.id.pieceAnim);
        ivCell[yMove2][xMove2].
                setImageDrawable(drawable.getDrawable());

    }

    /**
     * Show ending
     */
    @SuppressLint("NewApi")
    private void finalDialog() {
        /* Show this when the game is over */
        // declare dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.game_completed);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // access to dialog content view
        View lt = dialog.getWindow().getDecorView();

        // TextView block
        TextView winner = lt.findViewById(R.id.tvWinner);
        TextView conqueror = lt.findViewById(R.id.conqueror);

        // Button block
        Button Restart = lt.findViewById(R.id.btnRestart);
        Button Quit = lt.findViewById(R.id.btnQuit);

        conqueror.setText(R.string.dialog2_message);
        // Setters
        if (Core.GetWinner() == Game.Player.WHITE)
            winner.setText(R.string.player_white);
        else if (Core.GetWinner() == Game.Player.BLACK)
            winner.setText(R.string.player_black);
        else {
            conqueror.setText(R.string.final_game_state);
            winner.setText(R.string.draft);
        }

        isGameRunning = false;

        Restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                showRadioButtonDialog();
            }
        });

        Quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                finish();
            }
        });

        dialog.show();
    }

    /**
     * @return board size in pixels
     */
    private float boardViewWidth() {
        /* Use this to take the size of the board */
        // get display metrics
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        // get offset (dp --> int)
        int OFFSET_CONSTANT = 44; // board offset on screen
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OFFSET_CONSTANT,
                context.getResources().getDisplayMetrics());

        // return view width
        return (float) (dm.widthPixels - px);
    }
}
