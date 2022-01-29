package info.nightscout.androidaps.plugins.constraints.objectives;

public enum EducationObjective {
    CONFIG("config"),
    USAGE("usage"),
    EXAM("exam"),
    OPEN_LOOP("openloop"),
    MAX_BASAL("maxbasal"),
    MAX_IOB_ZERO("maxiobzero"),
    MAX_IOB("maxiob"),
    AUTO_SENS("autosens"),
    AMA("ama"),
    SMB("smb"),
    AUTOMATION("auto");

    private final String name;

    EducationObjective(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getHumanValue() {
        return this.ordinal() + 1;
    }
}
