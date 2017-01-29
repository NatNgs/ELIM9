import quickml.data.AttributesMap;
import quickml.data.instances.ClassifierInstance;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForest;
import quickml.supervised.ensembles.randomForest.randomDecisionForest.RandomDecisionForestBuilder;
import quickml.supervised.tree.attributeIgnoringStrategies.IgnoreAttributesWithConstantProbability;
import quickml.supervised.tree.decisionTree.DecisionTreeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathael on 29/01/17.
 */
public class RandomForest {
    private final List<ClassifierInstance> dataset = new ArrayList<>();
    private RandomDecisionForest randomForest;


    /**
     *
     * @param buildModel
     * @param screenOnDischargeDuration (minutes)
     * @param screenOnChargeDuration (minutes)
     * @param screenOffDischargeDuration (minutes)
     * @param screenOffChargeDuration (minutes)
     * @param averageUsedRam (Ko)
     * @return
     */
    public static AttributesMap makeAttMap(String buildModel,
                                           double screenOnDischargeDuration,
                                           double screenOnChargeDuration,
                                           double screenOffDischargeDuration,
                                           double screenOffChargeDuration,
                                           long   averageUsedRam) {
        AttributesMap map = new AttributesMap();
        map.put("BuildModel", buildModel);
        map.put("ScreenOnDischargeDuration", screenOnDischargeDuration);
        map.put("ScreenOnChargeDuration", screenOnChargeDuration);
        map.put("ScreenOffDischargeDuration", screenOffDischargeDuration);
        map.put("ScreenOffChargeDuration", screenOffChargeDuration);
        map.put("AverageUsedRam", averageUsedRam);
        return map;
    }
    public void feed(AttributesMap map, boolean feedClass) {
        ClassifierInstance ci = new ClassifierInstance(map, feedClass);
        dataset.add(ci);
        randomForest = null;
    }

    public synchronized String test(AttributesMap attributes) {
        if(randomForest == null) {
            randomForest = new RandomDecisionForestBuilder<>(new DecisionTreeBuilder<>()
                    // The default isn't desirable here because this dataset has so few attributes
                    .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.2))) // TODO: understanding this "0.2"
                    .buildPredictiveModel(dataset);
        }

        System.out.println("Prediction: " + randomForest.predict(attributes));
        for (ClassifierInstance instance : dataset) {
            System.out.println("classification: " + randomForest.getClassificationByMaxProb(instance.getAttributes()));
        }

        // TODO
        return "no result now";
    }
}
