package com.eldritch.hydrok.level;

public class TutorialProgress {
    enum Stage {
        Move("Press anywhere to move");
        
        private final String info;
        
        private Stage(String info) {
            this.info = info;
        }
        
        public String getInfo() {
            return info;
        }
    }
    
    private Stage stage = Stage.Move;
    
    public String getCurrentInfo() {
        return stage.getInfo();
    }
}
