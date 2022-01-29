package info.nightscout.androidaps.plugins.constraints.objectives.objectives;

import dagger.android.HasAndroidInjector;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.plugins.constraints.objectives.EducationObjective;

public class Objective4 extends Objective {

    public Objective4(HasAndroidInjector injector) {
        super(injector, EducationObjective.MAX_BASAL, R.string.objectives_maxbasal_objective, R.string.objectives_maxbasal_gate);
    }
}
