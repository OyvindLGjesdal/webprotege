package edu.stanford.bmir.protege.web.client.form;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import edu.stanford.bmir.protege.web.shared.form.data.*;
import edu.stanford.bmir.protege.web.shared.form.field.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 31/03/16
 */
public class ComboBoxChoiceControl extends Composite implements SingleChoiceControl {

    private SingleChoiceControlDescriptorDto descriptor;

    interface ComboBoxChoiceControlUiBinder extends UiBinder<HTMLPanel, ComboBoxChoiceControl> {

    }

    private static ComboBoxChoiceControlUiBinder ourUiBinder = GWT.create(ComboBoxChoiceControlUiBinder.class);

    @UiField
    ListBox comboBox;

    @UiField
    Label readOnlyLabel;

    private final List<PrimitiveFormControlData> choices = new ArrayList<>();

    private Optional<PrimitiveFormControlData> defaultChoice = Optional.empty();

    @Inject
    public ComboBoxChoiceControl() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("comboBox")
    protected void handleValueChanged(ChangeEvent changeEvent) {
        ValueChangeEvent.fire(this, getValue());
        updateReadonlyLabel();
    }

    public void setChoices(List<ChoiceDescriptorDto> choices) {
        List<PrimitiveFormControlData> nextChoices = choices.stream()
                .map(d -> d.getValue().toPrimitiveFormControlData())
                .collect(toImmutableList());
        if(this.choices.equals(nextChoices)) {
            return;
        }
        comboBox.clear();
        this.choices.clear();
        comboBox.addItem("");
        String langTag = LocaleInfo.getCurrentLocale().getLocaleName();
        for(ChoiceDescriptorDto descriptor : choices) {
            this.choices.add(descriptor.getValue().toPrimitiveFormControlData());
            comboBox.addItem(descriptor.getLabel().get(langTag));
        }
        selectDefaultChoice();
    }

    @Override
    public void setDescriptor(@Nonnull SingleChoiceControlDescriptorDto descriptor) {
        this.descriptor = descriptor;
//        descriptor.getDefaultChoice().ifPresent(this::setDefaultChoice);
        setChoices(descriptor.getAvailableChoices());
    }

    private void setDefaultChoice(@Nonnull ChoiceDescriptor defaultChoice) {
        this.defaultChoice = Optional.of(defaultChoice.getValue());
    }

    private void selectDefaultChoice() {
        comboBox.setSelectedIndex(0);
        readOnlyLabel.setText("");
    }

    @Override
    public void setValue(@Nonnull FormControlDataDto value) {
        if(value instanceof SingleChoiceControlDataDto) {
            SingleChoiceControlDataDto choiceControlDataDto = (SingleChoiceControlDataDto) value;
            Optional<PrimitiveFormControlData> choice = choiceControlDataDto.getChoice().map(PrimitiveFormControlDataDto::toPrimitiveFormControlData);
            if(choice.isPresent()) {
                int index = 1;
                for(PrimitiveFormControlData c : choices) {
                    if(c.equals(choice.get())) {
                        comboBox.setSelectedIndex(index);
                        String itemText = comboBox.getItemText(index);
                        readOnlyLabel.setText(itemText);
                        break;
                    }
                    index++;
                }
            }
            else {
                clearValue();
            }
        }
        else {
            clearValue();
        }
    }

    private void updateReadonlyLabel() {
        int sel = comboBox.getSelectedIndex();
        if(sel == -1) {
            readOnlyLabel.setText("");
        }
        else {
            readOnlyLabel.setText(comboBox.getItemText(sel));
        }
    }

    @Override
    public void clearValue() {
        selectDefaultChoice();
    }

    @Nonnull
    @Override
    public ImmutableSet<FormRegionFilter> getFilters() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<FormControlData> getValue() {
        int selIndex = comboBox.getSelectedIndex();
        if(selIndex < 1) {
            return Optional.empty();
        }
        PrimitiveFormControlData choice = choices.get(selIndex - 1);
        return Optional.of(SingleChoiceControlData.get(descriptor.toFormControlDescriptor(), choice));
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Optional<FormControlData>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void requestFocus() {
        comboBox.setFocus(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
        comboBox.setVisible(enabled);
        readOnlyLabel.setVisible(!enabled);
    }

    @Override
    public boolean isEnabled() {
        return comboBox.isEnabled();
    }

    @Override
    public void setFormRegionFilterChangedHandler(@Nonnull FormRegionFilterChangedHandler handler) {

    }
}
