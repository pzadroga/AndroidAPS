package info.nightscout.androidaps.plugins.constraints.objectives

import androidx.fragment.app.FragmentActivity
import dagger.android.HasAndroidInjector
import info.nightscout.androidaps.Config
import info.nightscout.androidaps.R
import info.nightscout.androidaps.interfaces.*
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.plugins.constraints.objectives.EducationObjective.*
import info.nightscout.androidaps.plugins.constraints.objectives.objectives.*
import info.nightscout.androidaps.utils.DateUtil
import info.nightscout.androidaps.utils.alertDialogs.OKDialog
import info.nightscout.androidaps.utils.resources.ResourceHelper
import info.nightscout.androidaps.utils.sharedPreferences.SP
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectivesPlugin @Inject constructor(
    injector: HasAndroidInjector,
    aapsLogger: AAPSLogger,
    resourceHelper: ResourceHelper,
    private val activePlugin: ActivePluginProvider,
    private val sp: SP,
    private val config: Config

) : PluginBase(PluginDescription()
    .mainType(PluginType.CONSTRAINTS)
    .fragmentClass(ObjectivesFragment::class.qualifiedName)
    .alwaysEnabled(config.APS)
    .showInList(config.APS)
    .pluginIcon(R.drawable.ic_graduation)
    .pluginName(R.string.objectives)
    .shortName(R.string.objectives_shortname)
    .description(R.string.description_objectives),
    aapsLogger, resourceHelper, injector
), ConstraintsInterface {

    var objectives: MutableList<Objective> = ArrayList()

    public override fun onStart() {
        super.onStart()
        convertSP()
        setupObjectives()
    }

    override fun specialEnableCondition(): Boolean {
        return activePlugin.activePump.pumpDescription.isTempBasalCapable
    }

    // convert 2.3 SP version
    private fun convertSP() {
        doConvertSP(0, "config")
        doConvertSP(1, "openloop")
        doConvertSP(2, "maxbasal")
        doConvertSP(3, "maxiobzero")
        doConvertSP(4, "maxiob")
        doConvertSP(5, "autosens")
        doConvertSP(6, "ama")
        doConvertSP(7, "smb")
    }

    private fun doConvertSP(number: Int, name: String) {
        if (!sp.contains("Objectives_" + name + "_started")) {
            sp.putLong("Objectives_" + name + "_started", sp.getLong("Objectives" + number + "started", 0L))
            sp.putLong("Objectives_" + name + "_accomplished", sp.getLong("Objectives" + number + "accomplished", 0L))
        }
        // TODO: we can remove Objectives1accomplished sometimes later
    }

    private fun setupObjectives() {
        objectives.clear()
        objectives.add(Objective0(injector))
        objectives.add(Objective1(injector))
        objectives.add(Objective2(injector))
        objectives.add(Objective3(injector))
        objectives.add(Objective4(injector))
        objectives.add(Objective5(injector))
        objectives.add(Objective6(injector))
        objectives.add(Objective7(injector))
        objectives.add(Objective8(injector))
        objectives.add(Objective9(injector))
        objectives.add(Objective10(injector))
    }

    private fun setupObjectivesComplete() {
        objectives.clear()
        objectives.add(Objective0(injector))
        objectives.add(Objective1(injector))
        objectives.add(Objective2(injector, true))
        objectives.add(Objective3(injector))
        objectives.add(Objective4(injector))
        objectives.add(Objective5(injector))
        objectives.add(Objective6(injector))
        objectives.add(Objective7(injector))
        objectives.add(Objective8(injector))
        objectives.add(Objective9(injector))
        objectives.add(Objective10(injector))
    }

    fun reset() {
        for (objective in objectives) {
            objective.startedOn = 0
            objective.accomplishedOn = 0
        }
        sp.putBoolean(R.string.key_ObjectivesbgIsAvailableInNS, false)
        sp.putBoolean(R.string.key_ObjectivespumpStatusIsAvailableInNS, false)
        sp.putInt(R.string.key_ObjectivesmanualEnacts, 0)
        sp.putBoolean(R.string.key_objectiveuseprofileswitch, false)
        sp.putBoolean(R.string.key_objectiveusedisconnect, false)
        sp.putBoolean(R.string.key_objectiveusereconnect, false)
        sp.putBoolean(R.string.key_objectiveusetemptarget, false)
        sp.putBoolean(R.string.key_objectiveuseactions, false)
        sp.putBoolean(R.string.key_objectiveuseloop, false)
        sp.putBoolean(R.string.key_objectiveusescale, false)
    }

    fun completeObjectives(activity: FragmentActivity, request: String) {
        // val requestCode = sp.getString(R.string.key_objectives_request_code, "")
        // var url = sp.getString(R.string.key_nsclientinternal_url, "").toLowerCase(Locale.getDefault())
        // if (!url.endsWith("/")) url = "$url/"
        // @Suppress("DEPRECATION") val hashNS = Hashing.sha1().hashString(url + BuildConfig.APPLICATION_ID + "/" + requestCode, Charsets.UTF_8).toString()
        if (request.equals("dupa", ignoreCase = true)) {
            for (objective in objectives) {
                if (objective.objectiveNumber > CONFIG.ordinal && objective.objectiveNumber <= AUTO_SENS.ordinal) {
                    objective.startedOn = DateUtil.now()
                    objective.accomplishedOn = DateUtil.now()
                }
            }
            setupObjectivesComplete()
            OKDialog.show(activity, resourceHelper.gs(R.string.objectives), resourceHelper.gs(R.string.codeaccepted))
        } else {
            OKDialog.show(activity, resourceHelper.gs(R.string.objectives), resourceHelper.gs(R.string.codeinvalid))
        }
    }

    fun allPriorAccomplished(position: Int): Boolean {
        var accomplished = true
        for (i in 0 until position) {
            accomplished = accomplished && objectives[i].isAccomplished
        }
        return accomplished
    }

    /**
     * Constraints interface
     */
    override fun isLoopInvocationAllowed(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[CONFIG.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), CONFIG.humanValue), this)
        return value
    }

    fun isLgsAllowed(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[MAX_BASAL.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), MAX_BASAL.humanValue), this)
        return value
    }

    override fun isClosedLoopAllowed(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[MAX_IOB_ZERO.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), MAX_IOB_ZERO.humanValue), this)
        return value
    }

    override fun isAutosensModeEnabled(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[AUTO_SENS.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), AUTO_SENS.humanValue), this)
        return value
    }

    override fun isAMAModeEnabled(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[AMA.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), AMA.humanValue), this)
        return value
    }

    override fun isSMBModeEnabled(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[SMB.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), SMB.humanValue), this)
        return value
    }

    override fun applyMaxIOBConstraints(maxIob: Constraint<Double>): Constraint<Double> {
        if (objectives[MAX_IOB_ZERO.ordinal].isStarted && !objectives[MAX_IOB_ZERO.ordinal].isAccomplished)
            maxIob.set(aapsLogger, 0.0, String.format(resourceHelper.gs(R.string.objectivenotfinished), MAX_IOB_ZERO.humanValue), this)
        return maxIob
    }

    override fun isAutomationEnabled(value: Constraint<Boolean>): Constraint<Boolean> {
        if (!objectives[AUTOMATION.ordinal].isStarted)
            value.set(aapsLogger, false, String.format(resourceHelper.gs(R.string.objectivenotstarted), AUTOMATION.humanValue), this)
        return value
    }
}
