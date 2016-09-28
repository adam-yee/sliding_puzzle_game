package com.mikeriv.ssui_2016.puzzlegame;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameBoard;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameState;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameTile;
import com.mikeriv.ssui_2016.puzzlegame.util.PuzzleImageUtil;
import com.mikeriv.ssui_2016.puzzlegame.view.PuzzleGameTileView;

import org.w3c.dom.Text;

import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {

    // The default grid size to use for the puzzle game 4 => 4x4 grid
    private static final int DEFAULT_PUZZLE_BOARD_SIZE = 4;

    // The id of the image to use for our puzzle game
    private static final int TILE_IMAGE_ID = R.drawable.duck;

    /**
     * Button Listener that starts a new game
     */
    private final View.OnClickListener mNewGameButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO start a new game if a new game button is clicked
            startNewGame();
        }
    };

    /**
     * Button Listener that pulls up the solution to the current puzzle
     */
    private PopupWindow mPopupWindow;
    private final View.OnClickListener mViewSolutionButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_solution, null);
            ImageView imageView = (ImageView) container.findViewById(R.id.imageView);
            imageView.setImageResource(0);
            imageView.setImageResource(mImageId);

            mPopupWindow = new PopupWindow(container,1000,1000,true);
            mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent){
                    mPopupWindow.dismiss();
                    //mPopupWindow = null;
                    return true;
                }
            });

        }
    };

    /**
     * Click Listener that Handles Tile Swapping when we click on a tile that is
     * neighboring the empty tile
     */
    private final View.OnClickListener mGameTileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           // TODO handle swapping tiles and updating the tileViews if there is a valid swap
            // with an empty tile
            // If any changes happen, be sure to update the state of the game to check for a win
            // condition

            // Don't allow clicks if game hasn't started
            if (mGameState != PuzzleGameState.PLAYING){
                return;
            }

            int rowsCount = mPuzzleGameBoard.getRowsCount();
            int colsCount = mPuzzleGameBoard.getColumnsCount();

            PuzzleGameTileView puzzleGameTileView = (PuzzleGameTileView) view;
            int tileId = puzzleGameTileView.getTileId();
            int tileRow = tileId/rowsCount;
            int tileCol = tileId%colsCount;

            // First check if the tile clicked on is Empty,
            // then check all of its neighbors for Empty
            if (!mPuzzleGameBoard.getTile(tileRow, tileCol).isEmpty()){
                // Check tile above for Empty
                if (tileRow - 1 >= 0){
                    if (mPuzzleGameBoard.getTile(tileRow-1, tileCol).isEmpty()){
                        mPuzzleGameBoard.swapTiles(tileRow, tileCol, tileRow - 1, tileCol);
                        updateGameState();
                        return;
                    }
                }
                // Check tile below for Empty
                if (tileRow + 1 < rowsCount){
                    if (mPuzzleGameBoard.getTile(tileRow+1, tileCol).isEmpty()){
                        mPuzzleGameBoard.swapTiles(tileRow, tileCol, tileRow + 1, tileCol);
                        updateGameState();
                        return;
                    }
                }
                // Check tile left for Empty
                if (tileCol - 1 >= 0){
                    if (mPuzzleGameBoard.getTile(tileRow, tileCol-1).isEmpty()){
                        mPuzzleGameBoard.swapTiles(tileRow, tileCol, tileRow, tileCol-1);
                        updateGameState();
                        return;
                    }
                }
                // Check tile right for Empty
                if (tileCol + 1 < colsCount){
                    if (mPuzzleGameBoard.getTile(tileRow, tileCol+1).isEmpty()){
                        mPuzzleGameBoard.swapTiles(tileRow, tileCol, tileRow, tileCol+1);
                        updateGameState();
                        return;
                    }
                }
            }
        }
    };

    // Game State - what the game is currently doin
    private PuzzleGameState mGameState = PuzzleGameState.NONE;

    // The size of our puzzle board (mPuzzleBoardSize x mPuzzleBoardSize grid)
    private int mPuzzleBoardSize = DEFAULT_PUZZLE_BOARD_SIZE;

    // The puzzleboard model
    private PuzzleGameBoard mPuzzleGameBoard;

    // Score Keeper
    private int mCurrentScore = 0;

    // Image Keeper
    private int mImageId = R.drawable.duck;

    // Views
    private LinearLayout mMainLayout;
    private LinearLayout mTableRow1;
    private LinearLayout mTableRow2;
    private LinearLayout mTableRow3;
    private LinearLayout mTableRow4;
    private Button mNewGameButton;
    private Button mViewSolutionButton;
    private TextView mStatusTextView;
    private TextView mScoreTextView;
    private Spinner mImageSelector;


    // The views for the puzzleboardtile models
    private PuzzleGameTileView[][] mPuzzleTileViews =
            new PuzzleGameTileView[mPuzzleBoardSize][mPuzzleBoardSize];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO initialize references to any containers views or layout groups etc.
        mMainLayout = (LinearLayout) findViewById(R.id.linear_main);
        mTableRow1 = (LinearLayout) findViewById(R.id.tablerow_1);
        mTableRow2 = (LinearLayout) findViewById(R.id.tablerow_2);
        mTableRow3 = (LinearLayout) findViewById(R.id.tablerow_3);
        mTableRow4 = (LinearLayout) findViewById(R.id.tablerow_4);

        mNewGameButton = (Button) findViewById(R.id.btn_new_game);
        mNewGameButton.setOnClickListener(mNewGameButtonOnClickListener);

        mViewSolutionButton = (Button) findViewById(R.id.btn_view_solution);
        mViewSolutionButton.setOnClickListener(mViewSolutionButtonOnClickListener);

        mStatusTextView = (TextView) findViewById(R.id.txt_status);
        mStatusTextView.setText(R.string.game_status_start);

        mScoreTextView = (TextView) findViewById(R.id.txt_score);
        String updatedScoreText = getString(R.string.title_score_board, mCurrentScore);
        mScoreTextView.setText(updatedScoreText);

        mImageSelector = (Spinner) findViewById(R.id.spinner_image_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.images_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mImageSelector.setAdapter(adapter);

        // Initializes the game and updates the game state
        initGame();
        // TODO createPuzzleTileViews with the appropriate width, height
        createPuzzleTileViews(0, 0);
    }

    /**
     * Creates the puzzleboard and the PuzzleGameTiles that serve as the model for the game. It
     * does image slicing to get the appropriate bitmap subdivisions of the TILE_IMAGE_ID. It
     * then creates a set for PuzzleGameTileViews that are used to display the information in models
     */
    private void initGame() {
        if (mPuzzleGameBoard != null)
            mPuzzleGameBoard.reset();
        else
            mPuzzleGameBoard = new PuzzleGameBoard(mPuzzleBoardSize, mPuzzleBoardSize);

        // Figure out which image to use
        String imageSelected = mImageSelector.getSelectedItem().toString();
        if (imageSelected == getString(R.string.image_duck)) {
            mImageId = R.drawable.duck;
        }
        else if (imageSelected == getString(R.string.image_pig)){
            mImageId = R.drawable.pig;
        }
        else if (imageSelected == getString(R.string.image_doggy)){
            mImageId = R.drawable.doggy;
        }

        // Get the original image bitmap

        Bitmap fullImageBitmap = BitmapFactory.decodeResource(getResources(), mImageId);
        // Now scale the bitmap so it fits our screen dimensions and change aspect ratio (scale) to
        // fit a square
        int fullImageWidth = fullImageBitmap.getWidth();
        int fullImageHeight = fullImageBitmap.getHeight();
        int squareImageSize = (fullImageWidth > fullImageHeight) ? fullImageWidth : fullImageHeight;
        fullImageBitmap = Bitmap.createScaledBitmap(
                fullImageBitmap,
                squareImageSize,
                squareImageSize,
                false);

        // TODO calculate the appropriate size for each puzzle tile
        int squareTileSide = squareImageSize / DEFAULT_PUZZLE_BOARD_SIZE;

        // TODO create the PuzzleGameTiles for the PuzzleGameBoard using sections of the bitmap.
        // You may find PuzzleImageUtil helpful for getting sections of the bitmap
        // Also ensure the last tile (the bottom right tile) is set to be an "empty" tile
        // (i.e. not filled with an section of the original image)
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        for (int i = 0; i < rowsCount; i++){
            for (int j = 0; j < colsCount; j++){
                Bitmap bitmapSection = PuzzleImageUtil.getSubdivisionOfBitmap(fullImageBitmap, squareTileSide, squareTileSide, i, j);
                Drawable drawableSection = new BitmapDrawable(Resources.getSystem(), bitmapSection);
                PuzzleGameTile gameTile = new PuzzleGameTile((i*rowsCount)+j, drawableSection);
                gameTile.setOrderIndex(i*rowsCount + j);
                mPuzzleGameBoard.setTile(gameTile, i, j);
            }
        }
        resetEmptyTileLocation(squareTileSide);

        //createPuzzleTileViews(0, 0);
        fullImageBitmap.recycle();
        fullImageBitmap = null;
    }

    private void createPuzzleTileViews(int minTileViewWidth, int minTileViewHeight) {
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        // TODO Set up TileViews (that will be what the user interacts with)
        // Make sure each tileView gets a click listener for interaction
        // Be sure to set the appropriate LayoutParams so that your tileViews
        // So that they fit your gameboard properly
        mTableRow1.removeAllViews();
        mTableRow2.removeAllViews();
        mTableRow3.removeAllViews();
        mTableRow4.removeAllViews();

        for (int i = 0; i < rowsCount; i++){
            for (int j = 0; j < colsCount; j++){
                PuzzleGameTileView gameTileView = new PuzzleGameTileView(this, (i * rowsCount)+j, minTileViewWidth, minTileViewHeight);
                gameTileView.setOnClickListener(mGameTileOnClickListener);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                gameTileView.setLayoutParams(params);

                if (i == 0) {
                    mTableRow1.addView(gameTileView);
                }
                else if (i == 1) {
                    mTableRow2.addView(gameTileView);
                }
                else if (i == 2) {
                    mTableRow3.addView(gameTileView);
                }
                else if (i == 3) {
                    mTableRow4.addView(gameTileView);
                }
                mPuzzleTileViews[i][j] = gameTileView;
            }
        }

        // updateGameState will update the displayed Tiles in each TileView
        updateGameState();
    }

    /**
     * Shuffles the puzzle tiles using a modified Durstenfield shuffle to ensure the board is
     * solvable
     */
    private void shufflePuzzleTiles() {
        // TODO randomly shuffle the tiles such that tiles may only move spots if it is randomly
        // swapped with a neighboring tile

        // Start from beginning picture
        initGame();

        Random rand = new Random();
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        int totalCount = rowsCount * colsCount - 1;

        for (int i = totalCount-1; i > 0; i--){
            int startingRow = (totalCount-1)/rowsCount;
            int staringCol = (totalCount-1)%colsCount;

            int randomInt = rand.nextInt(i);
            int randomRow = randomInt/rowsCount;
            int randomCol = randomInt%colsCount;
            mPuzzleGameBoard.swapTiles(startingRow,staringCol,randomRow,randomCol);
        }

        /* attempt 1
        for (int i = totalCount; i > 0; i--){
            int startingRow = i/rowsCount;
            int staringCol = i%colsCount;

            int randomInt = rand.nextInt(i);
            int randomRow = randomInt/rowsCount;
            int randomCol = randomInt%colsCount;

            if (mPuzzleGameBoard.areTilesNeighbors(startingRow,staringCol,randomRow,randomCol)){
                if (mPuzzleGameBoard.getTile(startingRow, staringCol).isEmpty() || mPuzzleGameBoard.getTile(randomRow,randomCol).isEmpty()) {
                    mPuzzleGameBoard.swapTiles(startingRow, staringCol, randomRow, randomCol);
                }
            }
            //updateGameState();
        }
        */
    }

    /**
     * Places the empty tile in the lower right corner of the grid
     */
    private void resetEmptyTileLocation(int squareTileSide) {
        // TODO
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();

        mPuzzleGameBoard.getTile(rowsCount-1, colsCount-1).setIsEmpty(true);
        Bitmap greyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grey);
        Drawable greyDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(greyBitmap, squareTileSide, squareTileSide, true));
        mPuzzleGameBoard.getTile(rowsCount-1, colsCount-1).setDrawable(greyDrawable);
    }

    /**
     * Updates the game state by checking if the user has won. Also triggers the tileViews to update
     * their visuals based on the gameboard
     */
    private void updateGameState() {
        // TODO refresh tiles and handle winning the game and updating score
        refreshGameBoardView();
        if (hasWonGame()){
            mGameState = PuzzleGameState.WON;
            mStatusTextView.setText(R.string.game_status_won);
            mCurrentScore += 1;
            updateScore();
        }
    }

    private void refreshGameBoardView() {
        // TODO update the PuzzleTileViews with the data stored in the PuzzleGameBoard
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();

        for (int i = 0; i < rowsCount; i++){
            for (int j = 0; j < colsCount; j++){
                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i,j));
            }
        }
    }

    /**
     * Checks the game board to see if the tile indices are in proper increasing order
     * @return true if the tiles are in correct order and the game is won
     */
    private boolean hasWonGame() {
        // TODO
        if (mGameState != PuzzleGameState.PLAYING) {
            return false;
        }
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();

        for (int i = 0; i < rowsCount; i++){
            for (int j = 0; j < colsCount; j++){
                if (!(mPuzzleGameBoard.getTile(i, j).getOrderIndex() == i*rowsCount + j)){
                    return false;
                }

            }
        }
        return true;
    }

    /**
     * Updates the score displayed in the text view
     */
    private void updateScore() {
        // TODO update a score to be displayed to the user
        String updatedScoreText = getString(R.string.title_score_board, mCurrentScore);
        mScoreTextView.setText(updatedScoreText);
    }

    /**
     * Begins a new game by shuffling the puzzle tiles, changing the game state to playing
     * and showing a start message
     */
    private void startNewGame() {
        // TODO - handle starting a new game by shuffling the tiles and showing a start message
        mGameState = PuzzleGameState.NONE;
        shufflePuzzleTiles();
        updateGameState();
        mGameState = PuzzleGameState.PLAYING;
        mStatusTextView.setText(R.string.game_status_playing);
    }


}
