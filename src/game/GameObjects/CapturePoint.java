package game;

import java.lang.reflect.Array;

public class CapturePoint extends Circle {

    public int captureState = 2; //0 for team 0, 1 for team 1, 2 for neutral,3 for 0 captured, 4 for 1 captured
    public int captureProgress = 0; //0 to 20
    public int completionProgress = 0; //0 to 300
    public int resetTimer = 30;
    public int prevCaptureState = -1;
    private Integer[][] spawnPoints;

    public CapturePoint(int mapDim) {
        super("", mapDim/2, mapDim/2);
        spawnPoints = new Integer[][] {
            {mapDim/2, mapDim/2}, {mapDim/2, mapDim/4}, {mapDim/2, 3*mapDim/4}, {mapDim/2, 200}, {mapDim/2, mapDim-200},
            {mapDim/3, mapDim/2}, {mapDim/3, mapDim/4}, {mapDim/3, 3*mapDim/4}, {mapDim/3, 200}, {mapDim/3, mapDim-200},
            {2*mapDim/3, mapDim/2}, {2*mapDim/3, mapDim/4}, {2*mapDim/3, 3*mapDim/4}, {2*mapDim/3, 200}, {2*mapDim/3, mapDim-200}
        };
        this.radius = 200;
    }

    public String updateCaptureState (int team0, int team1) {
        System.out.println("team0: "+team0+" team1: "+team1+" captureState: "+captureState+" captureProgress: "+captureProgress+" completionProgress: "+completionProgress);
        if (captureState == 2) {
            if (team1 > 0 && team0 == 0) {
                captureProgress++;
                if (captureProgress >= 20) {
                    captureState = 1;
                    captureProgress = 0;
                    completionProgress = 0;
                }
                return "Team 1 Capturing: "+(int)(captureProgress*5)+"%";
            } else if (team0 > 0 && team1 == 0) {
                captureProgress++;
                if (captureProgress >= 20) {
                    captureState = 0;
                    captureProgress = 0;
                    completionProgress = 0;
                }
                return "Team 0 Capturing: "+(int)(captureProgress*5)+"%";
            } else if (team0 > 0 && team1 > 0) {
                return "Contested";
            } else if (team0 == 0 && team1 == 0) {
                captureProgress = 0;
                if (prevCaptureState == -1) {
                    captureState = 2;
                } else {
                    captureState = prevCaptureState;
                }
            }
        } else if (captureState == 0) {
            if (team0 == 0 && team1 > 0) {
                captureState = 2;
                return "Team 1 Capturing: "+(int)(captureProgress*5)+"%";
            } else if (team1 > 0) {
                return "Contested";
            } else if (team0 == 0) {
                // no one present, fall through to switch
            } else {
                completionProgress += Math.log(team0)+1;
                prevCaptureState = 0;
                if (completionProgress >= 300) {
                    captureState = 3;
                }
            }
        } else if (captureState == 1) {
            if (team1 == 0 && team0 > 0) {
                captureState = 2;
                return "Team 0 Capturing: "+(int)(captureProgress*5)+"%";
            } else if (team0 > 0) {
                return "Contested";
            } else if (team1 == 0) {
                // no one present, fall through to switch
            } else {
                completionProgress += Math.log(team1)+1;
                prevCaptureState = 1;
                if (completionProgress >= 300) {
                    captureState = 4;
                }
            }
        } else if (captureState == 3 || captureState == 4) {
            resetTimer--;
            if (resetTimer <= 0) {
                reset();
            }
        }
        switch (captureState) {
            case 0:
                return "Team 0 Capturing with "+team0+" players: "+(int)(completionProgress/3)+"%";
            case 1:
                return "Team 1 Capturing with "+team1+" players: "+(int)(completionProgress/3)+"%";
            case 2:
                return "Neutral";
            case 3:
                return "Team 0 Captured";
            case 4:
                return "Team 1 Captured";
            default:
                return "";
        }
    }

    public void reset() {
        captureState = 2;
        captureProgress = 0;
        completionProgress = 0;
        resetTimer = 30;
        prevCaptureState = -1;

        Integer[] newpoint = spawnPoints[(int)(Math.random()*spawnPoints.length)];
        this.x = newpoint[0];
        this.y = newpoint[1];
    }

    public int getBarPercentage() {
        if (captureState == 0 || captureState == 1) {
            return (int)(completionProgress/2);
        } else if (captureState == 2) {
            return (int)(captureProgress*5);
        } else {
            return 0;
        }
    }
}
