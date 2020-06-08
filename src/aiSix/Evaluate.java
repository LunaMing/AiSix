package aiSix;


public class Evaluate {
    private static final int SIX = 500000;
    private static final int HUO_FIVE = 50000;
    private static final int MIAN_FIVE = 10000;
    private static final int HUO_FOUR = 5000;
    private static final int MIAN_FOUR = 1000;
    private static final int HUO_THREE = 500;
    private static final int MENGLONG_THREE = 300;
    private static final int MIAN_THREE = 100;
    private static final int HUO_TWO = 100;
    private static final int MIAN_TWO = 50;

    private static final int LARGE_NUMBER = 10000000;
    private static int SEARCH_DEPTH = 5;
    private static int SAMPLE_NUMBER = 10;


    private ChessBoard chessBoard;
    private int[][] blackValue;   // 保存每一空位下黑子的价值
    private int[][] whiteValue;   // 保存每一空位下白子的价值
    private int[][] staticValue;  // 保存每一点的位置价值，越靠中心，价值越大

    /**
     * 构造函数
     * 对黑白价值、静态价值数组进行初始化
     */
    public Evaluate(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;

        blackValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];
        whiteValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];
        staticValue = new int[ChessBoard.COLS + 1][ChessBoard.ROWS + 1];

        //黑白价值
        //初始都是0
        for (int i = 0; i <= ChessBoard.COLS; i++) {
            for (int j = 0; j <= ChessBoard.ROWS; j++) {
                blackValue[i][j] = 0;
                whiteValue[i][j] = 0;
            }
        }

        //静态价值
        //对每一点的价值进行初始化，越靠中心价值越大
        for (int i = 0; i <= ChessBoard.COLS / 2; i++) {
            for (int j = 0; j <= ChessBoard.ROWS / 2; j++) {
                staticValue[i][j] = Math.min(i, j);//第一象限内，价值就是行列坐标只
                staticValue[ChessBoard.COLS - i][j] = staticValue[i][j];//对称
                staticValue[i][ChessBoard.ROWS - j] = staticValue[i][j];
                staticValue[ChessBoard.COLS - i][ChessBoard.ROWS - j] = staticValue[i][j];
            }
        }
    }

    /**
     * 获取空位价值
     * 扩大左右，对每个空的点进行估值，每个点的分值为四个方向分值之和。
     */
    private void getTheSpaceValues() {
        int left, top, right, bottom;
        //todo
        left = (chessBoard.left > 2) ? chessBoard.left - 2 : 0;
        top = (chessBoard.top > 2) ? chessBoard.top - 2 : 0;
        right = (chessBoard.right < ChessBoard.COLS - 1) ? chessBoard.right + 2 : ChessBoard.COLS;
        bottom = (chessBoard.bottom < ChessBoard.ROWS - 1) ? chessBoard.bottom + 2 : ChessBoard.ROWS;
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                //对棋盘的所有点循环
                blackValue[i][j] = 0;
                whiteValue[i][j] = 0;
                if (chessBoard.boardStatus[i][j] == 0) {
                    //如果是空位，进行估值
                    for (int m = 1; m <= 4; m++) {
                        //每个点的分值为四个方向分值之和
                        blackValue[i][j] += evaluateValue(1, i, j, m);
                        whiteValue[i][j] += evaluateValue(2, i, j, m);
                    }
                }
            }
        }
    }

    /**
     * 获取计算机的最佳下棋位置
     * 评估函数的入口
     *
     * @return 最佳位置的坐标
     */
    int[] getTheBestPosition() {
        //获取空位的价值
        getTheSpaceValues();

        int maxValue = -LARGE_NUMBER;
        int value;
        int[] position = new int[2];

        int[][] valuablePositions = getTheMostValuablePositions();

        for (int i = 0; i < valuablePositions.length; i++) {
            if (valuablePositions[i][2] >= SIX) {
                //已经连六
                position[0] = valuablePositions[i][0];
                position[1] = valuablePositions[i][1];
                break;
            }
            chessBoard.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = chessBoard.computerColor;
            int oldLeft = chessBoard.left; //改变LEFT、TOP等值
            int oldTop = chessBoard.top; //改变LEFT、TOP等值
            int oldRight = chessBoard.right; //改变LEFT、TOP等值
            int oldBottom = chessBoard.bottom; //改变LEFT、TOP等值
            if (chessBoard.left > valuablePositions[i][0]) chessBoard.left = valuablePositions[i][0];
            if (chessBoard.top > valuablePositions[i][1]) chessBoard.top = valuablePositions[i][1];
            if (chessBoard.right < valuablePositions[i][0]) chessBoard.right = valuablePositions[i][0];
            if (chessBoard.bottom < valuablePositions[i][1]) chessBoard.bottom = valuablePositions[i][1];

            value = min(SEARCH_DEPTH, -LARGE_NUMBER, LARGE_NUMBER);

            chessBoard.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;
            chessBoard.left = oldLeft;
            chessBoard.top = oldTop;
            chessBoard.right = oldRight;
            chessBoard.bottom = oldBottom;

            if (value > maxValue) {
                maxValue = value;
                position[0] = valuablePositions[i][0];
                position[1] = valuablePositions[i][1];
            }
        }
        return position;
    }

    /**
     * @param depth：搜索的深度
     * @return //todo
     */
    private int min(int depth, int alpha, int beta) {
        if (depth == 0) { //如果搜索到最底层，直接返回当前的估值。
            return evaluateGame();
        }
        getTheSpaceValues();

        int[][] valuablePositions = getTheMostValuablePositions();

        for (int[] valuablePosition : valuablePositions) {
            chessBoard.boardStatus[valuablePosition[0]][valuablePosition[1]] = chessBoard.computerColor == 1 ? 2 : 1;
            int oldLeft = chessBoard.left;
            int oldTop = chessBoard.top;
            int oldRight = chessBoard.right;
            int oldBottom = chessBoard.bottom;
            if (chessBoard.left > valuablePosition[0]) chessBoard.left = valuablePosition[0];
            if (chessBoard.top > valuablePosition[1]) chessBoard.top = valuablePosition[1];
            if (chessBoard.right < valuablePosition[0]) chessBoard.right = valuablePosition[0];
            if (chessBoard.bottom < valuablePosition[1]) chessBoard.bottom = valuablePosition[1];

            int value = max(depth - 1, alpha, beta);

            chessBoard.boardStatus[valuablePosition[0]][valuablePosition[1]] = 0;
            chessBoard.left = oldLeft;
            chessBoard.top = oldTop;
            chessBoard.right = oldRight;
            chessBoard.bottom = oldBottom;

            if (value < beta) {
                beta = value;
                if (alpha >= beta) {
                    return alpha;
                }
            }
        }
        return beta;
    }

    /**
     * @param depth：搜索的深度
     * @return //todo
     */
    private int max(int depth, int alpha, int beta) {
        if (depth == 0) {
            //如果搜索到最底层，直接返回当前的估值。
            return evaluateGame();
        }
        getTheSpaceValues();

        int value;
        int[][] valuablePositions = getTheMostValuablePositions();

        for (int i = 0; i < valuablePositions.length; i++) {
            chessBoard.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = chessBoard.computerColor;
            int oldLeft = chessBoard.left;
            int oldTop = chessBoard.top;
            int oldRight = chessBoard.right;
            int oldBottom = chessBoard.bottom;
            if (chessBoard.left > valuablePositions[i][0]) chessBoard.left = valuablePositions[i][0];
            if (chessBoard.top > valuablePositions[i][1]) chessBoard.top = valuablePositions[i][1];
            if (chessBoard.right < valuablePositions[i][0]) chessBoard.right = valuablePositions[i][0];
            if (chessBoard.bottom < valuablePositions[i][1]) chessBoard.bottom = valuablePositions[i][1];

            value = min(depth - 1, alpha, beta);

            chessBoard.boardStatus[valuablePositions[i][0]][valuablePositions[i][1]] = 0;
            chessBoard.left = oldLeft;
            chessBoard.top = oldTop;
            chessBoard.right = oldRight;
            chessBoard.bottom = oldBottom;

            if (value > alpha) {
                alpha = value;
                if (alpha >= beta) {
                    return beta;
                }
            }
        }
        return alpha;
    }

    /**
     * 对数组按第三列（allValue[][2]降序排序)
     *
     * @param allValue: 待排序的数组，二维数组的前两列是棋盘位置坐标，第3列是该位置的价值
     */
    private void sort(int[][] allValue) {
        for (int i = 0; i < allValue.length; i++) {
            for (int j = 0; j < allValue.length - 1; j++) {
                int ti, tj, tvalue;
                if (allValue[j][2] < allValue[j + 1][2]) {
                    tvalue = allValue[j][2];
                    allValue[j][2] = allValue[j + 1][2];
                    allValue[j + 1][2] = tvalue;

                    ti = allValue[j][0];
                    allValue[j][0] = allValue[j + 1][0];
                    allValue[j + 1][0] = ti;

                    tj = allValue[j][1];
                    allValue[j][1] = allValue[j + 1][1];
                    allValue[j + 1][1] = tj;

                }
            }
        }
    }

    /**
     * 计算棋盘上指定空位在指定方向价值
     *
     * @param color     要计算的是哪一方的价值，1：黑方，2：白方
     * @param colomn    要计算位置的列坐标
     * @param row       要计算位置的行坐标
     * @param direction 要计算方向，1：水平，2：垂直，3：左上到右下，4：右上到左下
     * @return 价值
     */
    private int evaluateValue(int color, int colomn, int row, int direction) {
        int k, m;
        int value = 0;
        int chessCount1 = 1;  // 指定颜色的棋子数
        int chessCount2 = 0;  // 指定颜色的棋子数
        int chessCount3 = 0;  // 指定颜色的棋子数
        int spaceCount1 = 0;  //一端的空位数
        int spaceCountOtherSide1 = 0; //另一端空位数
        int spaceCountOtherSide2 = 0; //另一端空位数
        int spaceCountOtherSide3 = 0; //另一端空位数
        switch (direction) {
            case 1: //水平方向
                //向增加的方向查找相同颜色连续的棋子
                for (k = colomn + 1; k <= ChessBoard.COLS; k++) {
                    if (chessBoard.boardStatus[k][row] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((k <= ChessBoard.COLS) && (chessBoard.boardStatus[k][row] == 0)) {
                    spaceCount1++;
                    k++;
                }
                if (spaceCount1 == 1) {
                    while ((k <= ChessBoard.COLS) && (chessBoard.boardStatus[k][row] == color)) {
                        chessCount2++;
                        k++;
                    }
                    while ((k <= ChessBoard.COLS) && (chessBoard.boardStatus[k][row] == 0)) {
                        spaceCountOtherSide1++;
                        k++;
                    }
                }

                //向相反方向查找相同颜色连续的棋子
                for (k = colomn - 1; k >= 0; k--) {
                    if (chessBoard.boardStatus[k][row] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子的尽头查找连续的空格数
                while (k >= 0 && (chessBoard.boardStatus[k][row] == 0)) {
                    spaceCountOtherSide2++;
                    k--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((k >= 0) && (chessBoard.boardStatus[k][row] == color)) {
                        chessCount3++;
                        k--;
                    }
                    while ((k >= 0) && (chessBoard.boardStatus[k][row] == 0)) {
                        spaceCountOtherSide3++;
                        k--;
                    }
                }
                break;
            case 2:  //  垂直方向
                //向增加的方向查找相同颜色连续的棋子
                for (k = row + 1; k <= ChessBoard.ROWS; k++) {
                    if (chessBoard.boardStatus[colomn][k] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((k <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][k] == 0)) {
                    spaceCount1++;
                    k++;
                }
                if (spaceCount1 == 1) {
                    while ((k <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][k] == color)) {
                        chessCount2++;
                        k++;
                    }
                    while ((k <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][k] == 0)) {
                        spaceCountOtherSide1++;
                        k++;
                    }
                }

                //向相反方向查找相同颜色连续的棋子
                for (k = row - 1; k >= 0; k--) {
                    if (chessBoard.boardStatus[colomn][k] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在相反方向的棋子尽头查找连续的空格数
                while (k >= 0 && (chessBoard.boardStatus[colomn][k] == 0)) {
                    spaceCountOtherSide2++;
                    k--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((k >= 0) && (chessBoard.boardStatus[colomn][k] == color)) {
                        chessCount3++;
                        k--;
                    }
                    while ((k >= 0) && (chessBoard.boardStatus[colomn][k] == 0)) {
                        spaceCountOtherSide3++;
                        k--;
                    }
                }
                break;
            case 3:  //  左上到右下
                //向增加的方向查找相同颜色连续的棋子
                for (k = colomn + 1, m = row + 1; (k <= ChessBoard.COLS) && (m <= ChessBoard.ROWS); k++, m++) {
                    if (chessBoard.boardStatus[k][m] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((k <= ChessBoard.COLS) && (m <= ChessBoard.ROWS) && (chessBoard.boardStatus[k][m] == 0)) {
                    spaceCount1++;
                    k++;
                    m++;
                }
                if (spaceCount1 == 1) {
                    while ((k <= ChessBoard.COLS) && (m <= ChessBoard.ROWS) && (chessBoard.boardStatus[k][m] == color)) {
                        chessCount2++;
                        k++;
                        m++;
                    }
                    while ((k <= ChessBoard.COLS) && (m <= ChessBoard.ROWS) && (chessBoard.boardStatus[k][m] == 0)) {
                        spaceCountOtherSide1++;
                        k++;
                        m++;
                    }
                }

                //向相反方向查找相同颜色连续的棋子
                for (k = colomn - 1, m = row - 1; (k >= 0) && (m >= 0); k--, m--) {
                    if (chessBoard.boardStatus[k][m] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在相反方向的棋子尽头查找连续的空格数
                while ((k >= 0) && (m >= 0) && (chessBoard.boardStatus[k][m] == 0)) {
                    spaceCountOtherSide2++;
                    k--;
                    m--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((k >= 0) && (m >= 0) && (chessBoard.boardStatus[k][m] == color)) {
                        chessCount3++;
                        k--;
                        m--;
                    }
                    while ((k >= 0) && (m >= 0) && (chessBoard.boardStatus[k][m] == 0)) {
                        spaceCountOtherSide3++;
                        k--;
                        m--;
                    }
                }
                break;

            case 4:  //  右上到左下
                for (k = colomn + 1, m = row - 1; k <= ChessBoard.COLS && m >= 0; k++, m--) {  //查找连续的同色棋子
                    if (chessBoard.boardStatus[k][m] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                while (k <= ChessBoard.COLS && m >= 0 && (chessBoard.boardStatus[k][m] == 0)) { //统计空位数
                    spaceCount1++;
                    k++;
                    m--;
                }
                if (spaceCount1 == 1) {
                    while ((k <= ChessBoard.COLS) && (m >= 0) && (chessBoard.boardStatus[k][m] == color)) {
                        chessCount2++;
                        k++;
                        m--;
                    }
                    while ((k <= ChessBoard.COLS) && (m >= 0) && (chessBoard.boardStatus[k][m] == 0)) {
                        spaceCountOtherSide1++;
                        k++;
                        m--;
                    }
                }

                for (k = colomn - 1, m = row + 1; k >= 0 && m <= ChessBoard.ROWS; k--, m++) {  //查找连续的同色棋子
                    if (chessBoard.boardStatus[k][m] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                while (k >= 0 && m <= ChessBoard.ROWS && (chessBoard.boardStatus[k][m] == 0)) { // 统计空位数
                    spaceCountOtherSide2++;
                    k--;
                    m++;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((k >= 0) && (m <= ChessBoard.ROWS) && (chessBoard.boardStatus[k][m] == color)) {
                        chessCount3++;
                        k--;
                        m++;
                    }
                    while ((k >= 0) && (m <= ChessBoard.ROWS) && (chessBoard.boardStatus[k][m] == 0)) {
                        spaceCountOtherSide3++;
                        k--;
                        m++;
                    }
                }
                break;
        }
        if (chessCount1 + chessCount2 + chessCount3 + spaceCount1 + spaceCountOtherSide1 + spaceCountOtherSide2 + spaceCountOtherSide3 >= 6) {
            //只有同色棋子数加两端的空位数不少于6时，才有价值
            value = getValue(chessCount1, chessCount2, chessCount3, spaceCount1, spaceCountOtherSide1, spaceCountOtherSide2, spaceCountOtherSide3);
        }
        return value;
    }

    /**
     * 查找棋盘上价值最大的几个空位，每个空位的价值等于两种棋的价值之和。
     *
     * @return 价值最大的几个空位（包括位置和估值）
     */
    private int[][] getTheMostValuablePositions() {
        int i, j, k = 0;
        //allValue：保存每一空位的价值(列坐标，行坐标，价值）
        int[][] allValue = new int[(ChessBoard.COLS + 1) * (ChessBoard.ROWS + 1)][3];
        for (i = 0; i < ChessBoard.COLS; i++) {
            for (j = 0; j < ChessBoard.ROWS; j++) {
                if (chessBoard.boardStatus[i][j] == 0) {
                    allValue[k][0] = i;
                    allValue[k][1] = j;
                    allValue[k][2] = blackValue[i][j] + whiteValue[i][j] + staticValue[i][j];
                    k++;
                }
            }
        }
        sort(allValue);   //按价值降序排序

        int size = Math.min(k, SAMPLE_NUMBER);
        int[][] valuablePositions = new int[size][3];

        //将allValue中的前size个空位赋给bestPositions
        for (i = 0; i < size; i++) {
            valuablePositions[i][0] = allValue[i][0];
            valuablePositions[i][1] = allValue[i][1];
            valuablePositions[i][2] = allValue[i][2];
        }
        return valuablePositions;
    }

    /**
     * 静态评估
     */
    private int evaluateGame() {
        int value = 0;
        int[] line = new int[ChessBoard.COLS + 1];
        //水平  对每一行估值
        for (int j = 0; j <= ChessBoard.ROWS; j++) {
            for (int i = 0; i <= ChessBoard.COLS; i++) {
                line[i] = chessBoard.boardStatus[i][j];   //第一个下标是列下标
            }
            value += evaluateLine(line, ChessBoard.COLS + 1, 1);
            value -= evaluateLine(line, ChessBoard.COLS + 1, 2);
        }
        //竖直 对每一列估值
        for (int i = 0; i <= ChessBoard.COLS; i++) {
            for (int j = 0; j <= ChessBoard.ROWS; j++) {
                line[j] = chessBoard.boardStatus[i][j];
            }
            value += evaluateLine(line, ChessBoard.ROWS + 1, 1);
            value -= evaluateLine(line, ChessBoard.ROWS + 1, 2);
        }
        // 左下到右上斜线估值
        for (int j = 4; j <= ChessBoard.ROWS; j++) {
            for (int k = 0; k <= j; k++) {
                line[k] = chessBoard.boardStatus[k][j - k];
            }
            value += evaluateLine(line, j + 1, 1);
            value -= evaluateLine(line, j + 1, 2);
        }
        for (int j = 1; j <= ChessBoard.ROWS - 4; j++) {
            for (int k = 0; k <= ChessBoard.COLS - j; k++) {
                line[k] = chessBoard.boardStatus[k + j][ChessBoard.ROWS - k];
            }
            value += evaluateLine(line, ChessBoard.ROWS + 1 - j, 1);
            value -= evaluateLine(line, ChessBoard.ROWS + 1 - j, 2);
        }
        // 左上到右下斜线估值
        for (int j = 0; j <= ChessBoard.ROWS - 4; j++) {
            for (int k = 0; k <= ChessBoard.ROWS - j; k++) {
                line[k] = chessBoard.boardStatus[k][k + j];
            }
            value += evaluateLine(line, ChessBoard.ROWS + 1 - j, 1);
            value -= evaluateLine(line, ChessBoard.ROWS + 1 - j, 2);
        }
        for (int i = 1; i <= ChessBoard.COLS - 4; i++) {
            for (int k = 0; k <= ChessBoard.ROWS - i; k++) {
                line[k] = chessBoard.boardStatus[k + i][k];
            }
            value += evaluateLine(line, ChessBoard.ROWS + 1 - i, 1);
            value -= evaluateLine(line, ChessBoard.ROWS + 1 - i, 2);
        }
        if (chessBoard.computerColor == 1) {
            return value;
        } else {
            //另一方是负值
            return -value;
        }
    }

    private int evaluateLine(int[] lineState, int num, int color) {
        int chess, space1, space2;
        int value = 0;
        int begin, end;
        for (int i = 0; i < num; i++)
            if (lineState[i] == color) {
                //遇到要找的棋子，检查棋型，得到对应的分值
                chess = 1;
                begin = i;
                for (int j = begin + 1; (j < num) && (lineState[j] == color); j++) {
                    chess++;
                }
                if (chess < 2) {
                    continue;
                }
//                end = j - 1;//todo ???
                end = num - 1;

                space1 = 0;
                space2 = 0;
                for (int j = begin - 1; (j >= 0) && ((lineState[j] == 0) || (lineState[j] == color)); j--) {
                    //棋子前面的空格
                    space1++;
                }
                for (int j = end + 1; (j < num) && ((lineState[j] == 0) || (lineState[j] == color)); j++) {
                    //棋子前面的空格
                    space2++;
                }

                if (chess + space1 + space2 >= 6) {
                    value += getValue(chess, space1, space2);
                }
                i = end + 1;
            }
        return value;
    }

    /**
     * 获取不同棋型的价值
     * 只看一层
     * 将六子棋棋型分为连六、活五、眠五、活四、眠四、活三、朦胧三、眠三、活二、眠二
     *
     * @param chessCount          该空位置下一个棋子后同种颜色棋子连续的个数 AAA
     * @param spaceCount          连续棋子一端的连续空位数 AOOOO
     * @param spaceCountOtherSide 连续棋子另一端的连续空位数
     * @return 该点放棋子会带来的价值
     */
    private int getValue(int chessCount, int spaceCount, int spaceCountOtherSide) {
        int value = 0;
        //将六子棋棋型分为连六、活五、眠五、活四、眠四、活三、朦胧三、眠三、活二、眠二
        switch (chessCount) {
            case 6:
                //如果已经可以连成6子，则赢棋
                value = SIX;
                break;
            case 5:
                if ((spaceCount > 0) && (spaceCountOtherSide > 0)) {
                    //活五
                    value = HUO_FIVE;
                }
                break;
            case 4:
                if ((spaceCount > 0) && (spaceCountOtherSide > 0)) {
                    //活四
                    value = HUO_FOUR;
                }
                break;
            case 3:
                if ((spaceCount > 0) && (spaceCountOtherSide > 0)) {
                    //活三
                    value = HUO_THREE;
                }
                break;
            case 2:
                if ((spaceCount > 0) && (spaceCountOtherSide > 0)) {
                    //活二
                    value = HUO_TWO;
                }
                break;
            default:
                value = 0;
                break;
        }
        return value;
    }

    /**
     * 根据棋型，计算该点下一个棋子的价值
     * 看两层，也就是防止 AAOA 的情况
     *
     * @param chessCount1          该空位置下一个棋子后同种颜色棋子连续的个数 A后边连续的A个数
     * @param spaceCount1          连续棋子一端的连续空位数 A...AOOO O的个数
     * @param chessCount2          如果spaceCount1 = 1，继续连续同种颜色棋子的个数 AOAAAA 后边的A的个数
     * @param spaceCount2          继chessCount2之后，连续空位数 AOA..AOOOO O的个数
     * @param spaceCountOtherSide1 连续棋子另一端的连续空位数
     * @param chessCountOtherSide1 如果spaceCountOtherSide1 = 1，继续连续同种颜色棋子的个数
     * @param spaceCountOtherSide2 继chessCountOtherSide1之后，连续空位数
     * @return 在该点放棋子会带来的价值
     */
    private int getValue(int chessCount1, int chessCount2, int chessCountOtherSide1,
                         int spaceCount1, int spaceCount2, int spaceCountOtherSide1,
                         int spaceCountOtherSide2) {
        int value = 0;
        //将六子棋棋型分为连六、活五、眠五、活四、眠四、活三、朦胧三、眠三、活二、眠二
        //棋型：用A表示本颜色棋子，用B表示对方棋子，用O表示空位
        switch (chessCount1) {
            case 6:
                //如果连续棋子数量为6，已经可以连成6子，则赢棋
                //AAAAAA
                value = SIX;
                break;
            case 5:
                //如果连续棋子数量为5
                if ((spaceCount1 > 0) && (spaceCountOtherSide1 > 0)) {
                    //活五，也就是两边都空着，两方空白都大于零
                    //OAAAAAO
                    value = HUO_FIVE;
                } else if (((spaceCount1 == 0) && (spaceCountOtherSide1 > 0)) ||
                        ((spaceCount1 > 0) && (spaceCountOtherSide1 == 0))) {
                    //眠五
                    //一边有空位一边到头了
                    //OAAAAAB
                    value = MIAN_FIVE;
                }
                break;
            case 4:
                if ((spaceCount1 > 1) && (spaceCountOtherSide1 > 1)) {
                    //活四
                    value = HUO_FOUR;
                } else if (((spaceCount1 > 1) && (spaceCountOtherSide1 == 0)) ||
                        ((spaceCount1 == 0) && (spaceCountOtherSide1 > 1))) {
                    //眠四
                    value = MIAN_FOUR;
                }
                break;
            case 3:
                if ((spaceCount1 > 2) && (spaceCountOtherSide1 > 2)) {
                    //OOOAAAOOO
                    value = HUO_THREE;
                } else if (((spaceCount1 == 0) && (spaceCountOtherSide1 > 3)) ||
                        ((spaceCountOtherSide1 > 3) && (chessCountOtherSide1 == 0))) {
                    //AAAOOO
                    value = MIAN_THREE;
                }
                break;
            case 2:
                if ((spaceCount1 > 3) && (spaceCountOtherSide1 > 3)) {
                    //活二
                    value = HUO_TWO;
                } else if (((spaceCount1 > 3) && (spaceCountOtherSide1 == 0)) ||
                        ((spaceCount1 == 0) && (spaceCountOtherSide1 > 3))) {
                    //眠二
                    value = MIAN_TWO;
                } else if (((spaceCount1 == 1) && (chessCount2 == 1) && (spaceCount2 == 2) && (spaceCountOtherSide1 == 1)) ||
                        ((spaceCount1 == 1) && (chessCountOtherSide1 == 1) && (spaceCountOtherSide1 == 1) && (spaceCountOtherSide2 == 2))) {
                    //BOOAOAAOB
                    value = MENGLONG_THREE;
                }
                break;
            case 1:
                if (((spaceCount1 == 2) && (spaceCountOtherSide1 == 1) && (chessCountOtherSide1 == 2) && (spaceCountOtherSide2 == 1)) ||
                        ((spaceCount1 == 1) && (spaceCount2 == 1) && (chessCount2 == 2) && (spaceCountOtherSide1 == 2))) {
                    //BOOAOAAOB
                    value = MENGLONG_THREE;
                }
                break;
            default:
                value = 0;
                break;
        }
        return value;
    }

}

