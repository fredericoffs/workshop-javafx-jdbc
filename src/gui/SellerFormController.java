package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField textId;

	@FXML
	private TextField textName;

	@FXML
	private TextField textEmail;

	@FXML
	private DatePicker datePickerBirthDate;

	@FXML
	private TextField textBaseSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorEmail;

	@FXML
	private Label labelErrorBirthDate;

	@FXML
	private Label labelErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			// inserir ou atualizar no banco de dados
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	// responsável por pegar os dados que estão nas labels do form
	private Seller getFormData() {
		Seller obj = new Seller();

		ValidationException exception = new ValidationException("Validation Exception");

		// caso não seja int deixar vazio
		obj.setId(Utils.tryParseToInt(textId.getText()));

		// valida o campo textName ValidationException
		if (textName.getText() == null || textName.getText().equals("")) {
			exception.addError("name", "Field can`t be empty");
		}

		obj.setName(textName.getText());

		if (textEmail.getText() == null || textEmail.getText().equals("")) {
			exception.addError("email", "Field can`t be empty");
		}

		obj.setEmail(textEmail.getText());

		if (datePickerBirthDate.getValue() == null) {
			exception.addError("birthDate", "Field can`t be empty");
		} else {
			// Pegar o valor que está no datePicker no form
			Instant instant = Instant.from(datePickerBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			obj.setBirthDate(Date.from(instant));
		}

		if (textBaseSalary.getText() == null || textBaseSalary.getText().equals("")) {
			exception.addError("baseSalary", "Field can`t be empty");
		}

		// caso não seja double deixar vazio
		obj.setBaseSalary(Utils.tryParseToDouble(textBaseSalary.getText()));

		obj.setDepartment(comboBoxDepartment.getValue());

		// lança a exceção
		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(textId);
		Constraints.setTextFieldMaxLength(textName, 70);
		Constraints.setTextFieldDouble(textBaseSalary);
		Constraints.setTextFieldMaxLength(textEmail, 60);
		Utils.formatDatePicker(datePickerBirthDate, "dd/MM/yyyy");

		// inicializar o comboBox
		initializeComboBoxDepartment();
		
		// verifica se foi digitado a data ao invés de selecionado pelo datePicker
		Utils.onChangeDatePicker(datePickerBirthDate);
	}

	public void updateFormData() {

		// caso a variável esteja nula. defensiva
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}

		// O textId recebe String.
		// Converte o valor de int do Entint.getId() para String
		textId.setText(String.valueOf(entity.getId()));
		textName.setText(entity.getName());
		textEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		textBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));

		if (entity.getBirthDate() != null) {
			// pegar a data conforme a máquina do usuário
			datePickerBirthDate
					.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}

		if (entity.getDepartment() == null) {
			// caso for um cadastro e o departamento está vazia
			// trazer o primeiro cadastro para o combobox
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null.");
		}

		// carregar os departamentos do banco
		List<Department> list = departmentService.findAll();
		// carregar para a observableList
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		labelErrorName.setText((fields.contains("name")) ? errors.get("name") : "");
		labelErrorEmail.setText((fields.contains("email")) ? errors.get("email") : "");
		labelErrorBaseSalary.setText((fields.contains("baseSalary")) ? errors.get("baseSalary") : "");
		labelErrorBirthDate.setText((fields.contains("birthDate")) ? errors.get("birthDate") : "");
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
