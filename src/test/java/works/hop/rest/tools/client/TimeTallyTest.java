package works.hop.rest.tools.client;

import org.junit.Test;

public class TimeTallyTest {

    public int[] tallyTime(int hrs1, int min1, int sec1, int hrs2, int min2, int sec2) {
        int sec = sec1 + sec2;
        int min = min1 + min2;
        int hrs = hrs1 + hrs2;
        if (sec >= 60) {
            sec -= 60;
            min++;
        }
        if (min >= 60) {
            min -= 60;
            hrs++;
        }
        return new int[]{hrs, min, sec};
    }

    public int[] tallyTime(int[] time1, int[] time2) {
        return tallyTime(time1[0], time1[1], 0, time2[0], time2[1], 0);
    }

    @Test
    public void testTallyTime() {
        int[][] input = {
            {7, 10}, {8, 53}, {8, 21}, {4, 9}, {8, 3}, {9, 1}, {9, 9}, {9, 2}, {9, 36},
            {7, 49}, {8, 28}, {8, 33}, {3, 26}, {7, 33}, {6, 10}, {7, 45}, {7, 46}, {7, 33},
            {8, 33}, {6, 5}, {7, 48}, {7, 46}, {8, 32}, {7, 54}, {7, 46}, {8, 20}, {7, 51},
            {9, 4}, {8, 41}, {6, 30}, {6, 50}, {7, 22}, {8, 49}, {8, 27},
            {8, 19}, {9, 7}, {8, 30}, {7, 31}, {8, 11}, {5, 3}, {6, 42}, {8, 8}, {8, 45},
            {8, 45}, {8, 56}, {9, 2}, {8, 14}, {8, 36}};

        int tally[] = new int[]{0, 0};
        for (int[] input1 : input) {
            tally = tallyTime(tally, input1);
        }
        System.out.format("total time %d hrs, %d min\n", tally[0], tally[1]);
    }
}
