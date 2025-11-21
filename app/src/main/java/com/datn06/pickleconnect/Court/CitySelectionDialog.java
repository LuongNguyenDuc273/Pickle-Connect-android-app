package com.datn06.pickleconnect.Court;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datn06.pickleconnect.Adapter.CityAdapter;
import com.datn06.pickleconnect.Model.CityDTO;
import com.datn06.pickleconnect.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog để chọn Tỉnh/Thành phố từ danh sách
 * Có chức năng search để tìm kiếm nhanh
 */
public class CitySelectionDialog extends BottomSheetDialogFragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private MaterialButton btnCancel, btnOk;

    private CityAdapter adapter;
    private List<CityDTO> cityList;
    private List<CityDTO> filteredList;
    private CityDTO selectedCity;

    private OnCitySelectedListener listener;

    public interface OnCitySelectedListener {
        void onCitySelected(CityDTO city);
    }

    public static CitySelectionDialog newInstance(List<CityDTO> cities) {
        CitySelectionDialog dialog = new CitySelectionDialog();
        Bundle args = new Bundle();
        args.putSerializable("cityList", (Serializable) cities);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityList = (List<CityDTO>) getArguments().getSerializable("cityList");
            filteredList = new ArrayList<>(cityList);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_city_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnOk = view.findViewById(R.id.btnOk);

        setupRecyclerView();
        setupSearch();
        setupButtons();
    }

    private void setupRecyclerView() {
        adapter = new CityAdapter(filteredList, city -> {
            selectedCity = city;
            adapter.setSelectedCity(city);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCities(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(cityList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (CityDTO city : cityList) {
                if (city.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(city);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnOk.setOnClickListener(v -> {
            if (selectedCity != null && listener != null) {
                listener.onCitySelected(selectedCity);
            }
            dismiss();
        });
    }
}
