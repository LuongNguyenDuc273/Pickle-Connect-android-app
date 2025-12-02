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

    // ✅ NEW: Validate tất cả các field và trả về field đầu tiên bị lỗi
    public int validateAllFields() {
        android.util.Log.d("DynamicFormAdapter", "╔════════════════════════════════════════════════════════════╗");
        android.util.Log.d("DynamicFormAdapter", "║              validateAllFields() START                     ║");
        android.util.Log.d("DynamicFormAdapter", "╚════════════════════════════════════════════════════════════╝");
        android.util.Log.d("DynamicFormAdapter", "  Total fields: " + formFields.size());

        for (int i = 0; i < formFields.size(); i++) {
            TourneyRegConfigResponse field = formFields.get(i);

            android.util.Log.d("DynamicFormAdapter", "  [" + i + "] Checking field: " + field.getFieldName());
            android.util.Log.d("DynamicFormAdapter", "      Label: " + field.getLabel());
            android.util.Log.d("DynamicFormAdapter", "      Type: " + field.getFieldType());
            android.util.Log.d("DynamicFormAdapter", "      Required: " + field.getIsRequired());
            android.util.Log.d("DynamicFormAdapter", "      Value: '" + field.getValue() + "'");

            if (field.getIsRequired() != null && field.getIsRequired()) {
                String value = field.getValue();

                // Check if value is empty
                if (value == null || value.trim().isEmpty()) {
                    android.util.Log.e("DynamicFormAdapter", "  ✗ VALIDATION FAILED at position " + i);
                    android.util.Log.e("DynamicFormAdapter", "      Reason: Required field is empty");
                    return i; // Return position of first invalid field
                }

                // For checkbox, check if at least one is selected
                if ("checkbox".equalsIgnoreCase(field.getFieldType())) {
                    List<String> selectedValues = field.getSelectedValues();
                    android.util.Log.d("DynamicFormAdapter", "      Selected values: " + selectedValues);

                    if (selectedValues == null || selectedValues.isEmpty()) {
                        android.util.Log.e("DynamicFormAdapter", "  ✗ VALIDATION FAILED at position " + i);
                        android.util.Log.e("DynamicFormAdapter", "      Reason: Required checkbox has no selection");
                        return i;
                    }
                }

                // For select/spinner, check if default option is still selected
                if ("select".equalsIgnoreCase(field.getFieldType())) {
                    if (value.startsWith("Chọn ")) {
                        android.util.Log.e("DynamicFormAdapter", "  ✗ VALIDATION FAILED at position " + i);
                        android.util.Log.e("DynamicFormAdapter", "      Reason: Default option still selected");
                        return i;
                    }
                }

                android.util.Log.d("DynamicFormAdapter", "      ✓ Valid");
            } else {
                android.util.Log.d("DynamicFormAdapter", "      ⊘ Not required, skipped");
            }
        }

        android.util.Log.d("DynamicFormAdapter", "  ✓ All fields validated successfully");
        return -1; // All fields valid
    }

    // ✅ NEW: Get field at position để show error message
    public TourneyRegConfigResponse getFieldAt(int position) {
        if (position >= 0 && position < formFields.size()) {
            return formFields.get(position);
        }
        return null;
    }

    // ============================================
    // TextboxViewHolder
    // ============================================
    class TextboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        EditText etInput;
        TextView tvRequired;
        TextView tvError; // ✅ NEW: Error message view

        private TextWatcher textWatcher; // ✅ FIX: Store watcher to prevent multiple listeners

        public TextboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            etInput = itemView.findViewById(R.id.etInput);
            tvRequired = itemView.findViewById(R.id.tvRequired);
            // tvError = itemView.findViewById(R.id.tvError); // Add this to your layout if needed
        }

        public void bind(TourneyRegConfigResponse field) {
            // ✅ FIX: Remove old text watcher before adding new one
            if (textWatcher != null) {
                etInput.removeTextChangedListener(textWatcher);
            }

            // Set label
            tvLabel.setText(field.getLabel());

            // Show/hide required indicator
            tvRequired.setVisibility(
                    (field.getIsRequired() != null && field.getIsRequired()) ? View.VISIBLE : View.GONE
            );

            // Set input type based on field name
            setInputTypeByFieldName(field.getFieldName());

            // Set existing value
            String currentValue = field.getValue();
            if (!etInput.getText().toString().equals(currentValue != null ? currentValue : "")) {
                etInput.setText(currentValue);
            }

            // ✅ FIX: Create new text watcher
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String newValue = s.toString().trim();
                    field.setValue(newValue);

                    // ✅ NEW: Clear error when user types
                    if (tvError != null && !newValue.isEmpty()) {
                        tvError.setVisibility(View.GONE);
                    }

                    if (listener != null) {
                        listener.onFieldChanged(field);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            };

            etInput.addTextChangedListener(textWatcher);

            // ✅ NEW: Show error if field is invalid
            boolean isRequired = field.getIsRequired() != null && field.getIsRequired();
            if (isRequired && (currentValue == null || currentValue.trim().isEmpty())) {
                showError("Vui lòng nhập " + field.getLabel().toLowerCase());
            } else {
                hideError();
            }
        }

        private void showError(String message) {
            if (tvError != null) {
                tvError.setText(message);
                tvError.setVisibility(View.VISIBLE);
                etInput.setError(message); // Built-in Android error
            }
        }

        private void hideError() {
            if (tvError != null) {
                tvError.setVisibility(View.GONE);
            }
            etInput.setError(null);
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
        TextView tvError;

        private boolean isBinding = false; // ✅ FIX: Prevent listener trigger during bind

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            spinner = itemView.findViewById(R.id.spinner);
            tvRequired = itemView.findViewById(R.id.tvRequired);
            // tvError = itemView.findViewById(R.id.tvError);
        }

        public void bind(TourneyRegConfigResponse field) {
            isBinding = true; // ✅ Prevent listener trigger

            tvLabel.setText(field.getLabel());
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            List<String> options = field.getOptions();
            if (options != null && !options.isEmpty()) {
                // ✅ Add default "Chọn..." option
                List<String> spinnerOptions = new ArrayList<>();
                spinnerOptions.add("Chọn " + field.getLabel().toLowerCase());
                spinnerOptions.addAll(options);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        itemView.getContext(),
                        android.R.layout.simple_spinner_item,
                        spinnerOptions
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                // Set selected value if exists
                int selectedPosition = 0; // Default to first item
                if (field.getValue() != null && !field.getValue().isEmpty()) {
                    int position = options.indexOf(field.getValue());
                    if (position >= 0) {
                        selectedPosition = position + 1; // +1 because of default option
                    }
                }
                spinner.setSelection(selectedPosition);

                // ✅ FIX: Set listener after setting selection
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (isBinding) return; // Ignore during binding

                        if (position == 0) {
                            // Default option selected
                            field.setValue("");
                        } else {
                            field.setValue(options.get(position - 1));
                            if (tvError != null) {
                                tvError.setVisibility(View.GONE);
                            }
                        }

                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        field.setValue("");
                    }
                });
            }

            isBinding = false; // ✅ Re-enable listener
        }
    }

    // ============================================
    // RadioViewHolder
    // ============================================
    class RadioViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        RadioGroup radioGroup;
        TextView tvRequired;
        TextView tvError;

        private boolean isBinding = false;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            tvRequired = itemView.findViewById(R.id.tvRequired);
            // tvError = itemView.findViewById(R.id.tvError);
        }

        public void bind(TourneyRegConfigResponse field) {
            isBinding = true;

            tvLabel.setText(field.getLabel());
            tvRequired.setVisibility(field.getIsRequired() ? View.VISIBLE : View.GONE);

            radioGroup.removeAllViews();
            radioGroup.clearCheck();

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
                    if (isBinding) return;

                    RadioButton selected = group.findViewById(checkedId);
                    if (selected != null) {
                        field.setValue(selected.getText().toString());
                        if (tvError != null) {
                            tvError.setVisibility(View.GONE);
                        }
                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    }
                });
            }

            isBinding = false;
        }
    }

    // ============================================
    // CheckboxViewHolder
    // ============================================
    class CheckboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        LinearLayout checkboxContainer;
        TextView tvRequired;
        TextView tvError;

        private boolean isBinding = false;

        public CheckboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            checkboxContainer = itemView.findViewById(R.id.checkboxContainer);
            tvRequired = itemView.findViewById(R.id.tvRequired);
            // tvError = itemView.findViewById(R.id.tvError);
        }

        public void bind(TourneyRegConfigResponse field) {
            isBinding = true;

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
                        if (isBinding) return;

                        if (isChecked) {
                            if (!finalSelectedValues.contains(option)) {
                                finalSelectedValues.add(option);
                            }
                        } else {
                            finalSelectedValues.remove(option);
                        }

                        // Update value as comma-separated string
                        field.setValue(String.join(",", finalSelectedValues));

                        if (!finalSelectedValues.isEmpty() && tvError != null) {
                            tvError.setVisibility(View.GONE);
                        }

                        if (listener != null) {
                            listener.onFieldChanged(field);
                        }
                    });

                    checkboxContainer.addView(checkBox);
                }
            }

            isBinding = false;
        }
    }
}