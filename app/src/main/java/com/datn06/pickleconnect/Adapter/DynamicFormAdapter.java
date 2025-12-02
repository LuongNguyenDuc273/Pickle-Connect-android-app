package com.datn06.pickleconnect.Adapter;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.datn06.pickleconnect.Models.Tournament.TourneyRegConfigResponse;
import com.datn06.pickleconnect.R;
import java.util.ArrayList;
import java.util.List;

public class DynamicFormAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXTBOX = 1;
    private static final int TYPE_SELECT = 2;
    private static final int TYPE_RADIO = 3;
    private static final int TYPE_CHECKBOX = 4;

    private List<TourneyRegConfigResponse> formFields;
    private OnFieldValueChangedListener listener;

    public interface OnFieldValueChangedListener {
        void onFieldChanged(TourneyRegConfigResponse field);
    }

    public DynamicFormAdapter(List<TourneyRegConfigResponse> formFields,
                              OnFieldValueChangedListener listener) {
        this.formFields = formFields != null ? formFields : new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        String fieldType = formFields.get(position).getFieldType();

        if (fieldType == null) return TYPE_TEXTBOX;

        switch (fieldType.toLowerCase()) {
            case "textbox":
                return TYPE_TEXTBOX;
            case "select":
                return TYPE_SELECT;
            case "radio":
                return TYPE_RADIO;
            case "checkbox":
                return TYPE_CHECKBOX;
            default:
                return TYPE_TEXTBOX;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_TEXTBOX:
                return new TextboxViewHolder(
                        inflater.inflate(R.layout.item_form_textbox, parent, false)
                );
            case TYPE_SELECT:
                return new SelectViewHolder(
                        inflater.inflate(R.layout.item_form_select, parent, false)
                );
            case TYPE_RADIO:
                return new RadioViewHolder(
                        inflater.inflate(R.layout.item_form_radio, parent, false)
                );
            case TYPE_CHECKBOX:
                return new CheckboxViewHolder(
                        inflater.inflate(R.layout.item_form_checkbox, parent, false)
                );
            default:
                return new TextboxViewHolder(
                        inflater.inflate(R.layout.item_form_textbox, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TourneyRegConfigResponse field = formFields.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_TEXTBOX:
                ((TextboxViewHolder) holder).bind(field);
                break;
            case TYPE_SELECT:
                ((SelectViewHolder) holder).bind(field);
                break;
            case TYPE_RADIO:
                ((RadioViewHolder) holder).bind(field);
                break;
            case TYPE_CHECKBOX:
                ((CheckboxViewHolder) holder).bind(field);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return formFields.size();
    }

    public void updateFormFields(List<TourneyRegConfigResponse> newFields) {
        this.formFields = newFields != null ? newFields : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ============================================
    // TextboxViewHolder
    // ============================================
    class TextboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        EditText etInput;
        TextView tvRequired;

        public TextboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            etInput = itemView.findViewById(R.id.etInput);
            tvRequired = itemView.findViewById(R.id.tvRequired);
        }

        public void bind(TourneyRegConfigResponse field) {
            // Set label
            tvLabel.setText(field.getLabel());

            // Show/hide required indicator
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            // Set input type based on field name
            setInputTypeByFieldName(field.getFieldName());

            // Set existing value
            if (field.getValue() != null) {
                etInput.setText(field.getValue());
            }

            // Add text watcher
            etInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    field.setValue(s.toString());
                    if (listener != null) {
                        listener.onFieldChanged(field);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        private void setInputTypeByFieldName(String fieldName) {
            if (fieldName == null) return;

            fieldName = fieldName.toLowerCase();

            if (fieldName.contains("email")) {
                etInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if (fieldName.contains("phone") || fieldName.contains("số điện thoại")) {
                etInput.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if (fieldName.contains("number") || fieldName.contains("age")) {
                etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                etInput.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        }
    }

    // ============================================
    // SelectViewHolder (Spinner)
    // ============================================
    class SelectViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        Spinner spinner;
        TextView tvRequired;

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            spinner = itemView.findViewById(R.id.spinner);
            tvRequired = itemView.findViewById(R.id.tvRequired);
        }

        public void bind(TourneyRegConfigResponse field) {
            tvLabel.setText(field.getLabel());
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            List<String> options = field.getOptions();
            if (options != null && !options.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        itemView.getContext(),
                        android.R.layout.simple_spinner_item,
                        options
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                // Set selected value if exists
                if (field.getValue() != null) {
                    int position = options.indexOf(field.getValue());
                    if (position >= 0) {
                        spinner.setSelection(position);
                    }
                }

                // Listener
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        field.setValue(options.get(position));
                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        }
    }

    // ============================================
    // RadioViewHolder
    // ============================================
    class RadioViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        RadioGroup radioGroup;
        TextView tvRequired;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            tvRequired = itemView.findViewById(R.id.tvRequired);
        }

        public void bind(TourneyRegConfigResponse field) {
            tvLabel.setText(field.getLabel());
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            radioGroup.removeAllViews();

            List<String> options = field.getOptions();
            if (options != null && !options.isEmpty()) {
                for (int i = 0; i < options.size(); i++) {
                    String option = options.get(i);
                    RadioButton radioButton = new RadioButton(itemView.getContext());
                    radioButton.setText(option);
                    radioButton.setId(View.generateViewId());

                    // Check if this was previously selected
                    if (option.equals(field.getValue())) {
                        radioButton.setChecked(true);
                    }

                    radioGroup.addView(radioButton);
                }

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton selected = group.findViewById(checkedId);
                    if (selected != null) {
                        field.setValue(selected.getText().toString());
                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    }
                });
            }
        }
    }

    // ============================================
    // CheckboxViewHolder
    // ============================================
    class CheckboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        LinearLayout checkboxContainer;
        TextView tvRequired;

        public CheckboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            checkboxContainer = itemView.findViewById(R.id.checkboxContainer);
            tvRequired = itemView.findViewById(R.id.tvRequired);
        }

        public void bind(TourneyRegConfigResponse field) {
            tvLabel.setText(field.getLabel());
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            checkboxContainer.removeAllViews();

            List<String> selectedValues = field.getSelectedValues();
            if (selectedValues == null) {
                selectedValues = new ArrayList<>();
                field.setSelectedValues(selectedValues);
            }

            List<String> options = field.getOptions();
            if (options != null && !options.isEmpty()) {
                for (String option : options) {
                    CheckBox checkBox = new CheckBox(itemView.getContext());
                    checkBox.setText(option);

                    // Restore checked state
                    checkBox.setChecked(selectedValues.contains(option));

                    List<String> finalSelectedValues = selectedValues;
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            if (!finalSelectedValues.contains(option)) {
                                finalSelectedValues.add(option);
                            }
                        } else {
                            finalSelectedValues.remove(option);
                        }

                        // Update value as comma-separated string
                        field.setValue(String.join(",", finalSelectedValues));

                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    });

                    checkboxContainer.addView(checkBox);
                }
            }
        }
    }
}
