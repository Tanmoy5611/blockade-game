package blockaders.presenter.rules;

import blockaders.view.rules.RulesView;


public class RulesPresenter {
    // Main rules text shown at the top of the rules window
    private static final String RULES_SUMMARY =
            "Capture opponent's pieces by jumping over them, move horizontally or vertically, and block opponents to win the game.";

    private final RulesView view;

    // Connects the rules view to its static text
    public RulesPresenter(RulesView view) {
        this.view = view;
        initialize();
    }

    // Sets the main rules text
    public void initialize() {
        view.getRules().setText(RULES_SUMMARY);
    }
}