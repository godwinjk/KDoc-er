package com.kdocer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

/**
 * Created by Godwin on 8/1/2020 1:05 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
public class SettingsPanel {
    /**
     * The General level panel.
     */
    private JPanel generalLevelPanel;
    /**
     * The General level type checkbox.
     */
    private JCheckBox generalLevelTypeCheckbox;
    /**
     * The General level method checkbox.
     */
    private JCheckBox generalLevelMethodCheckbox;
    /**
     * The General level field checkbox.
     */
    private JCheckBox generalLevelFieldCheckbox;
    /**
     * The General visibility panel.
     */
    private JPanel generalVisibilityPanel;
    /**
     * The General visibility public checkbox.
     */
    private JCheckBox generalVisibilityPublicCheckbox;
    /**
     * The General visibility protected checkbox.
     */
    private JCheckBox generalVisibilityProtectedCheckbox;
    /**
     * The General visibility private checkbox.
     */
    private JCheckBox generalVisibilityPrivateCheckbox;
    /**
     * The General other panel.
     */
    private JPanel generalOtherPanel;
    /**
     * The General Other Enable Empty Constructor checkbox.
     */
    private JCheckBox generalOtherEmptyConstructor;
    /**
     * The General other overridden methods checkbox.
     */
    private JCheckBox generalOtherOverriddenMethodsCheckbox;
    /**
     * The General other splitted class name.
     */
    private JCheckBox generalOtherSplittedClassName;
    /**
     * The General panel.
     */
    private JPanel generalPanel;
    /**
     * The General mode panel.
     */
    private JPanel generalModePanel;
    /**
     * The General mode keep radio button.
     */
    private JRadioButton generalModeKeepRadioButton;
    /**
     * The General mode replace radio button.
     */
    private JRadioButton generalModeReplaceRadioButton;
    /**
     * The Panel.
     */
    private JPanel panel;
    /**
     * The General visibility internal.
     */
    private JCheckBox generalVisibilityInternal;
    private JCheckBox disableNotification;
    private JTextField lblInfo;
    private JLabel lblInfoDonate;
    private JLabel lblInfoRate;
    private JCheckBox generalOtherEnableEmptyConstructor;

    private ConfigCallback callback;

    /**
     * Instantiates a new Settings panel.
     */
    public SettingsPanel() {
        ButtonGroup group = new ButtonGroup();
        group.add(generalModeKeepRadioButton);
        group.add(generalModeReplaceRadioButton);

        lblInfoDonate.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://paypal.me/godwinj"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        lblInfoRate.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://plugins.jetbrains.com/plugin/14778-kdoc-er--kotlin-doc-generator"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    /**
     * Gets panel.
     *
     * @return the panel
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Sets allowed public.
     *
     * @param isAllowed the is allowed
     */
    public void setAllowedPublic(boolean isAllowed) {
        generalVisibilityPublicCheckbox.setSelected(isAllowed);
    }

    /**
     * Sets allowed private.
     *
     * @param isAllowed the is allowed
     */
    public void setAllowedPrivate(boolean isAllowed) {
        generalVisibilityPrivateCheckbox.setSelected(isAllowed);
    }

    /**
     * Sets allowed override.
     *
     * @param isAllowed the is allowed
     */
    public void setAllowedOverride(boolean isAllowed) {
        generalOtherOverriddenMethodsCheckbox.setSelected(isAllowed);
    }

    /**
     * Is allowed private boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedPrivate() {
        return generalVisibilityPrivateCheckbox.isSelected();
    }

    /**
     * Is allowed public boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedPublic() {
        return generalVisibilityPublicCheckbox.isSelected();
    }

    /**
     * Is allowed override boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedOverride() {
        return generalOtherOverriddenMethodsCheckbox.isSelected();
    }

    /**
     * Is allowed protected boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedProtected() {
        return generalVisibilityProtectedCheckbox.isSelected();
    }

    /**
     * Sets allowed protected.
     *
     * @param allowedProtected the allowed protected
     */
    public void setAllowedProtected(boolean allowedProtected) {
        generalVisibilityProtectedCheckbox.setSelected(allowedProtected);
    }

    /**
     * Is allowed internal boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedInternal() {
        return generalVisibilityInternal.isSelected();
    }

    /**
     * Sets allowed internal.
     *
     * @param allowedInternal the allowed internal
     */
    public void setAllowedInternal(boolean allowedInternal) {
        generalVisibilityInternal.setSelected(allowedInternal);
    }

    /**
     * Is allowed keep doc boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedKeepDoc() {
        return generalModeKeepRadioButton.isSelected();
    }

    /**
     * Sets allowed keep doc.
     *
     * @param allowedKeepDoc the allowed keep doc
     */
    public void setAllowedKeepDoc(boolean allowedKeepDoc) {
        generalModeKeepRadioButton.setSelected(allowedKeepDoc);
    }

    /**
     * Is allowed replace doc boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedReplaceDoc() {
        return generalModeReplaceRadioButton.isSelected();
    }

    /**
     * Sets allowed replace doc.
     *
     * @param allowedReplaceDoc the allowed replace doc
     */
    public void setAllowedReplaceDoc(boolean allowedReplaceDoc) {
        generalModeKeepRadioButton.setSelected(allowedReplaceDoc);
    }

    /**
     * Is splitted class names boolean.
     *
     * @return the boolean
     */
    public boolean isSplittedClassNames() {
        return generalOtherSplittedClassName.isSelected();
    }

    /**
     * Sets splitted class names.
     *
     * @param splittedClassNames the splitted class names
     */
    public void setSplittedClassNames(boolean splittedClassNames) {
        generalOtherSplittedClassName.setSelected(splittedClassNames);
    }

    /**
     * Is allowed empty constructor
     * @return the boolean
     */
    public boolean isAllowedEmptyConstructor() { return generalOtherEmptyConstructor.isSelected(); }

    /**
     * Sets allowed empty constructor
     *
     * @param isAllowed the is allowed
     */
    public void setAllowedEmptyConstructor(boolean isAllowed) { generalOtherEmptyConstructor.setSelected(isAllowed); }


    /**
     * Is allowed class boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedClass() {
        return generalLevelTypeCheckbox.isSelected();
    }

    /**
     * Sets allowed class.
     *
     * @param allowedClass the allowed class
     */
    public void setAllowedClass(boolean allowedClass) {
        generalLevelTypeCheckbox.setSelected(allowedClass);
    }

    /**
     * Is allowed fun boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedFun() {
        return generalLevelMethodCheckbox.isSelected();
    }

    /**
     * Sets allowed fun.
     *
     * @param allowedFun the allowed fun
     */
    public void setAllowedFun(boolean allowedFun) {
        generalLevelMethodCheckbox.setSelected(allowedFun);
    }

    /**
     * Is allowed field boolean.
     *
     * @return the boolean
     */
    public boolean isAllowedField() {
        return generalLevelFieldCheckbox.isSelected();
    }

    /**
     * Sets allowed field.
     *
     * @param allowedField the allowed field
     */
    public void setAllowedField(boolean allowedField) {
        generalLevelFieldCheckbox.setSelected(allowedField);
    }


    public boolean isDisabledNotification() {
        return disableNotification.isSelected();
    }


    public void setDisabledNotification(boolean allowedField) {
        disableNotification.setSelected(allowedField);
    }

    public ConfigCallback getCallback() {
        return callback;
    }

    public void setCallback(ConfigCallback callback) {
        this.callback = callback;
    }


    public interface ConfigCallback {
        void onDisabledNotification();
    }
}
