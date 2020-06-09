package aiSix;


public class Evaluate {
    /*---------------------棋型价值-----------------------*/
    //将六子棋棋型分为连六、活五、眠五、活四、眠四、活三、朦胧三、眠三、活二、眠二
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

    private static final int INFINITY = 10000000;//无穷大
    private static final int SEARCH_DEPTH = 5;//搜索深度
    private static final int MY_REAL_VALUABLE_POSITION_NUM = 10;//可选点的最多搜索数量

    private final ChessBoard chessBoard;// 输入的棋盘布局
    private final int[][] blackValue;   // 保存每一空位下黑子的价值
    private final int[][] whiteValue;   // 保存每一空位下白子的价值
    private final int[][] staticValue;  // 保存每一点的位置价值，越靠中心，价值越大

    /**
     * 构造函数
     * 对黑白价值、静态价值数组进行初始化
     *
     * @param chessBoard 当前布局
     */
    public Evaluate(ChessBoard chessBoard) {
        //当前布局
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
     * 获取计算机的最佳下棋位置
     * 评估函数的入口
     *
     * @return 最佳位置的坐标，先x后y
     */
    int[] getTheBestPosition() {
        //最佳位置的坐标，先x后y
        int[] position = new int[2];

        //获取所有可选点
        //获取每个格子的价值
        updateBlackAndWhiteValue();
        //按照价值排序产生可选点
        int[][] valuablePositions = getTheMostValuablePositions();

        //首先假设是最差情况
        int maxValue = -INFINITY;
        //遍历所有可选点，计算价值
        for (int[] valuablePosition : valuablePositions) {
            int x = valuablePosition[0];
            int y = valuablePosition[1];
            int v = valuablePosition[2];//价值

            //如果价值已经比连续的六还大，就直接下
            if (v >= SIX) {
                //说明已经连六
                position[0] = x;
                position[1] = y;
                break;
            }

            //假设先下了这一步棋
            chessBoard.boardStatus[x][y] = chessBoard.computerColor;

            //改变LEFT、TOP等值
            int oldLeft = chessBoard.left;
            int oldTop = chessBoard.top;
            int oldRight = chessBoard.right;
            int oldBottom = chessBoard.bottom;
            //边界检测
            if (chessBoard.left > x) chessBoard.left = x;
            if (chessBoard.top > y) chessBoard.top = y;
            if (chessBoard.right < x) chessBoard.right = x;
            if (chessBoard.bottom < y) chessBoard.bottom = y;

            //调用Alpha-Beta算法
            int value = min(SEARCH_DEPTH, -INFINITY, INFINITY);

            //撤回了之前下的棋子
            chessBoard.boardStatus[x][y] = 0;
            //将LEFT、TOP等值恢复原样
            chessBoard.left = oldLeft;
            chessBoard.top = oldTop;
            chessBoard.right = oldRight;
            chessBoard.bottom = oldBottom;

            //如果value比最大值还要大
            if (value > maxValue) {
                maxValue = value;
                position[0] = x;
                position[1] = y;
            }
        }

        return position;
    }

    /**
     * Alpha-Beta算法的min
     *
     * @param depth 搜索的深度
     * @return 最优价值
     */
    private int min(int depth, int alpha, int beta) {
        if (depth == 0) {
            //如果搜索到最底层，直接返回当前的估值。
            return evaluateGame();
        }

        updateBlackAndWhiteValue();

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
     * Alpha-Beta算法的min
     *
     * @param depth 搜索的深度
     * @return 最优价值
     */
    private int max(int depth, int alpha, int beta) {
        if (depth == 0) {
            //如果搜索到最底层，直接返回当前的估值。
            return evaluateGame();
        }

        updateBlackAndWhiteValue();

        int[][] valuablePositions = getTheMostValuablePositions();

        for (int[] valuablePosition : valuablePositions) {
            chessBoard.boardStatus[valuablePosition[0]][valuablePosition[1]] = chessBoard.computerColor;
            int oldLeft = chessBoard.left;
            int oldTop = chessBoard.top;
            int oldRight = chessBoard.right;
            int oldBottom = chessBoard.bottom;
            if (chessBoard.left > valuablePosition[0]) chessBoard.left = valuablePosition[0];
            if (chessBoard.top > valuablePosition[1]) chessBoard.top = valuablePosition[1];
            if (chessBoard.right < valuablePosition[0]) chessBoard.right = valuablePosition[0];
            if (chessBoard.bottom < valuablePosition[1]) chessBoard.bottom = valuablePosition[1];

            int value = min(depth - 1, alpha, beta);

            chessBoard.boardStatus[valuablePosition[0]][valuablePosition[1]] = 0;
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
     * Alpha-Beta的静态评估
     */
    private int evaluateGame() {
        int value = 0;
        int[] line = new int[ChessBoard.COLS + 1];
        //水平 对每一行估值
        for (int j = 0; j <= ChessBoard.ROWS; j++) {
            for (int i = 0; i <= ChessBoard.COLS; i++) {
                //第一个下标是列下标
                line[i] = chessBoard.boardStatus[i][j];
            }
            value += evaluateLine(line, ChessBoard.COLS + 1, 1);
            value -= evaluateLine(line, ChessBoard.COLS + 1, 2);
        }
        //垂直 对每一列估值
        for (int i = 0; i <= ChessBoard.COLS; i++) {
            System.arraycopy(chessBoard.boardStatus[i], 0, line, 0, ChessBoard.ROWS + 1);
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

    /**
     * 计算一行的连珠数，调用棋型，计算价值
     *
     * @param lineState 一行上的棋子
     * @param num       这一行棋子数量
     * @param color     要计算的是哪一方的价值，1：黑方，2：白方
     * @return 这一行的最终价值
     */
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
                    value += getValueByThree(chess, space1, space2);
                }
                i = end + 1;
            }
        return value;
    }

    /**
     * 根据棋型返回价值
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

    /*----------------------------排序选出可选点------------------------------*/

    /**
     * 查找可选点
     * 每个空位的价值等于黑白棋的价值加上位置本身的价值
     *
     * @return 价值最大的几个可选点，数组前面一层是[格子序号]，后面是{列坐标，行坐标，价值}
     */
    private int[][] getTheMostValuablePositions() {
        //所有格子的总数 = 行 * 列
        int allSquareNum = (ChessBoard.COLS + 1) * (ChessBoard.ROWS + 1);
        //保存每一格子的价值
        //外层索引是格子数，里面一层的三个分别存的是：列坐标，行坐标，价值
        //例如：allValue[0] = {column, row, value};
        int[][] allValue = new int[allSquareNum][3];
        //格子的索引
        int squareIndex = 0;
        //遍历所有格子
        for (int i = 0; i < ChessBoard.COLS; i++) {
            for (int j = 0; j < ChessBoard.ROWS; j++) {
                if (chessBoard.boardStatus[i][j] == 0) {
                    allValue[squareIndex][0] = i;
                    allValue[squareIndex][1] = j;
                    //价值 = 黑 + 白 + 静态位置
                    allValue[squareIndex][2] = blackValue[i][j] + whiteValue[i][j] + staticValue[i][j];
                    squareIndex++;
                }
            }
        }

        //按价值降序排序
        sort(allValue);

        //需要进行评估的可选点数量
        //从“格子总数”和“自定义的可选点数量”里面取最小值，就是实际用的可选点数量
        int realValuablePositionNum = Math.min(allSquareNum, MY_REAL_VALUABLE_POSITION_NUM);
        int[][] valuablePositions = new int[realValuablePositionNum][3];
        //按照自定义的选择数量，将有价值的点复制给可选点
        for (int i = 0; i < realValuablePositionNum; i++) {
            valuablePositions[i][0] = allValue[i][0];
            valuablePositions[i][1] = allValue[i][1];
            valuablePositions[i][2] = allValue[i][2];
        }
        return valuablePositions;
    }

    /**
     * 对可选点按照价值排序
     * 也就是对数组按第三列（allValue[][2]降序排序）
     *
     * @param allValue 待排序的数组，数组前面一层是[格子序号]，后面是{列坐标，行坐标，价值}
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


    /*----------------------------快速静态评估-----------------------------*/

    /**
     * 更新每格的黑白价值
     * 按照当前局面，给每个格子的黑白价值进行更新，每个点的分值为四个方向分值之和。
     * 调用了评估棋型，里面是还用数连珠数量，计算棋型价值的方法
     */
    private void updateBlackAndWhiteValue() {
        int left, top, right, bottom;
        left = (chessBoard.left > 2) ? chessBoard.left - 2 : 0;
        top = (chessBoard.top > 2) ? chessBoard.top - 2 : 0;
        right = (chessBoard.right < ChessBoard.COLS - 1) ? chessBoard.right + 2 : ChessBoard.COLS;
        bottom = (chessBoard.bottom < ChessBoard.ROWS - 1) ? chessBoard.bottom + 2 : ChessBoard.ROWS;
        for (int i = left; i <= right; i++) {
            for (int j = top; j <= bottom; j++) {
                //对棋盘的所有点循环
                if (chessBoard.boardStatus[i][j] == 0) {
                    //如果是空位，进行估值
                    for (int m = 1; m <= 4; m++) {
                        //每个点的分值为四个方向分值之和
                        //要计算的是哪一方的价值，1：黑方，2：白方
                        //要计算方向，1：水平，2：垂直，3：左上到右下，4：右上到左下
                        blackValue[i][j] += evaluateValue(1, i, j, m);
                        whiteValue[i][j] += evaluateValue(2, i, j, m);
                    }
                } else {
                    //如果不是空位，就没有价值
                    blackValue[i][j] = 0;
                    whiteValue[i][j] = 0;
                }
            }
        }
    }

    /**
     * 计算棋盘上可选点的价值
     * 包括颜色，坐标，方向
     *
     * @param color     要计算的是哪一方的价值，1：黑方，2：白方
     * @param colomn    要计算位置的列坐标
     * @param row       要计算位置的行坐标
     * @param direction 要计算方向，1：水平，2：垂直，3：左上到右下，4：右上到左下
     * @return 可选点的限定价值
     */
    private int evaluateValue(int color, int colomn, int row, int direction) {
        int columnCount, rowCount;
        int value = 0;
        int chessCount1 = 1;  // 指定颜色的棋子数
        int chessCount2 = 0;  // 指定颜色的棋子数
        int chessCount3 = 0;  // 指定颜色的棋子数
        int spaceCount1 = 0;  //一端的空位数
        int spaceCountOtherSide1 = 0; //另一端空位数
        int spaceCountOtherSide2 = 0; //另一端空位数
        int spaceCountOtherSide3 = 0; //另一端空位数
        switch (direction) {
            //水平方向
            case 1: {
                //向增加的方向查找相同颜色连续的棋子
                for (columnCount = colomn + 1; columnCount <= ChessBoard.COLS; columnCount++) {
                    if (chessBoard.boardStatus[columnCount][row] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((columnCount <= ChessBoard.COLS) && (chessBoard.boardStatus[columnCount][row] == 0)) {
                    spaceCount1++;
                    columnCount++;
                }
                if (spaceCount1 == 1) {
                    while ((columnCount <= ChessBoard.COLS) && (chessBoard.boardStatus[columnCount][row] == color)) {
                        chessCount2++;
                        columnCount++;
                    }
                    while ((columnCount <= ChessBoard.COLS) && (chessBoard.boardStatus[columnCount][row] == 0)) {
                        spaceCountOtherSide1++;
                        columnCount++;
                    }
                }
                //向相反方向查找相同颜色连续的棋子
                for (columnCount = colomn - 1; columnCount >= 0; columnCount--) {
                    if (chessBoard.boardStatus[columnCount][row] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子的尽头查找连续的空格数
                while (columnCount >= 0 && (chessBoard.boardStatus[columnCount][row] == 0)) {
                    spaceCountOtherSide2++;
                    columnCount--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((columnCount >= 0) && (chessBoard.boardStatus[columnCount][row] == color)) {
                        chessCount3++;
                        columnCount--;
                    }
                    while ((columnCount >= 0) && (chessBoard.boardStatus[columnCount][row] == 0)) {
                        spaceCountOtherSide3++;
                        columnCount--;
                    }
                }
                break;
            }
            //垂直方向
            case 2: {
                //向增加的方向查找相同颜色连续的棋子
                for (columnCount = row + 1; columnCount <= ChessBoard.ROWS; columnCount++) {
                    if (chessBoard.boardStatus[colomn][columnCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((columnCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][columnCount] == 0)) {
                    spaceCount1++;
                    columnCount++;
                }
                if (spaceCount1 == 1) {
                    while ((columnCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][columnCount] == color)) {
                        chessCount2++;
                        columnCount++;
                    }
                    while ((columnCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[colomn][columnCount] == 0)) {
                        spaceCountOtherSide1++;
                        columnCount++;
                    }
                }
                //向相反方向查找相同颜色连续的棋子
                for (columnCount = row - 1; columnCount >= 0; columnCount--) {
                    if (chessBoard.boardStatus[colomn][columnCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在相反方向的棋子尽头查找连续的空格数
                while (columnCount >= 0 && (chessBoard.boardStatus[colomn][columnCount] == 0)) {
                    spaceCountOtherSide2++;
                    columnCount--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((columnCount >= 0) && (chessBoard.boardStatus[colomn][columnCount] == color)) {
                        chessCount3++;
                        columnCount--;
                    }
                    while ((columnCount >= 0) && (chessBoard.boardStatus[colomn][columnCount] == 0)) {
                        spaceCountOtherSide3++;
                        columnCount--;
                    }
                }
                break;
            }
            //左上到右下
            case 3: {
                //向增加的方向查找相同颜色连续的棋子
                for (columnCount = colomn + 1, rowCount = row + 1; (columnCount <= ChessBoard.COLS) && (rowCount <= ChessBoard.ROWS); columnCount++, rowCount++) {
                    if (chessBoard.boardStatus[columnCount][rowCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在棋子尽头查找连续的空格数
                while ((columnCount <= ChessBoard.COLS) && (rowCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                    spaceCount1++;
                    columnCount++;
                    rowCount++;
                }
                if (spaceCount1 == 1) {
                    while ((columnCount <= ChessBoard.COLS) && (rowCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[columnCount][rowCount] == color)) {
                        chessCount2++;
                        columnCount++;
                        rowCount++;
                    }
                    while ((columnCount <= ChessBoard.COLS) && (rowCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                        spaceCountOtherSide1++;
                        columnCount++;
                        rowCount++;
                    }
                }
                //向相反方向查找相同颜色连续的棋子
                for (columnCount = colomn - 1, rowCount = row - 1; (columnCount >= 0) && (rowCount >= 0); columnCount--, rowCount--) {
                    if (chessBoard.boardStatus[columnCount][rowCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                //在相反方向的棋子尽头查找连续的空格数
                while ((columnCount >= 0) && (rowCount >= 0) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                    spaceCountOtherSide2++;
                    columnCount--;
                    rowCount--;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((columnCount >= 0) && (rowCount >= 0) && (chessBoard.boardStatus[columnCount][rowCount] == color)) {
                        chessCount3++;
                        columnCount--;
                        rowCount--;
                    }
                    while ((columnCount >= 0) && (rowCount >= 0) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                        spaceCountOtherSide3++;
                        columnCount--;
                        rowCount--;
                    }
                }
                break;
            }
            //右上到左下
            case 4: {
                for (columnCount = colomn + 1, rowCount = row - 1; columnCount <= ChessBoard.COLS && rowCount >= 0; columnCount++, rowCount--) {
                    //查找连续的同色棋子
                    if (chessBoard.boardStatus[columnCount][rowCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                while (columnCount <= ChessBoard.COLS && rowCount >= 0 && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                    //统计空位数
                    spaceCount1++;
                    columnCount++;
                    rowCount--;
                }
                if (spaceCount1 == 1) {
                    while ((columnCount <= ChessBoard.COLS) && (rowCount >= 0) && (chessBoard.boardStatus[columnCount][rowCount] == color)) {
                        chessCount2++;
                        columnCount++;
                        rowCount--;
                    }
                    while ((columnCount <= ChessBoard.COLS) && (rowCount >= 0) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                        spaceCountOtherSide1++;
                        columnCount++;
                        rowCount--;
                    }
                }
                for (columnCount = colomn - 1, rowCount = row + 1; columnCount >= 0 && rowCount <= ChessBoard.ROWS; columnCount--, rowCount++) {
                    //查找连续的同色棋子
                    if (chessBoard.boardStatus[columnCount][rowCount] == color) {
                        chessCount1++;
                    } else {
                        break;
                    }
                }
                while (columnCount >= 0 && rowCount <= ChessBoard.ROWS && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                    // 统计空位数
                    spaceCountOtherSide2++;
                    columnCount--;
                    rowCount++;
                }
                if (spaceCountOtherSide2 == 1) {
                    while ((columnCount >= 0) && (rowCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[columnCount][rowCount] == color)) {
                        chessCount3++;
                        columnCount--;
                        rowCount++;
                    }
                    while ((columnCount >= 0) && (rowCount <= ChessBoard.ROWS) && (chessBoard.boardStatus[columnCount][rowCount] == 0)) {
                        spaceCountOtherSide3++;
                        columnCount--;
                        rowCount++;
                    }
                }
                break;
            }
        }
        if (chessCount1 + chessCount2 + chessCount3 + spaceCount1 + spaceCountOtherSide1 + spaceCountOtherSide2 + spaceCountOtherSide3 >= 6) {
            //只有同色棋子数加两端的空位数不少于6时，才有价值
            //将棋的布局放入计算棋型的函数，获取棋型的真实价值
            value = getValue(chessCount1, chessCount2, chessCount3, spaceCount1, spaceCountOtherSide1, spaceCountOtherSide2, spaceCountOtherSide3);
        }
        return value;
    }

    /**
     * 获取不同棋型的价值
     * 只看一层，快速看
     *
     * @param chessCount          该空位置下一个棋子后同种颜色棋子连续的个数 AAA
     * @param spaceCount          连续棋子一端的连续空位数 AOOOO
     * @param spaceCountOtherSide 连续棋子另一端的连续空位数
     * @return 该点放棋子会带来的价值
     */
    private int getValueByThree(int chessCount, int spaceCount, int spaceCountOtherSide) {
        int value = 0;
        //六子棋棋型
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


}

