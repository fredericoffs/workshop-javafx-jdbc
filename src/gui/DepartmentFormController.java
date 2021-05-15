package gui;

import java.net.URL;
import java.util.ResourceBundle;

import gui.util.Constraints;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;

public class DepartmentFormController implements Initializable {

	private Department departmentEntity;

	@FXML
	private TextField textId;

	@FXML
	private TextField textName;

	@FXML
	private Label labelErrorName;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	public void setDepartment(Department departmentEntity) {
		this.departmentEntity = departmentEntity;
	}

	@FXML
	public void onBtSaveAction() {
		System.out.println("onBtSaveAction");
	}

	@FXML
	public void onBtCancelAction() {
		System.out.println("onBtCancelAction");
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(textId);
		Constraints.setTextFieldMaxLength(textName, 30);
	}

	public void updateFormData() {

		// caso a variável esteja nula. defensiva 
		if (departmentEntity == null) {
			throw new IllegalStateException("departmentEntity was null");
		}

		// O textId recebe String.
		// Converte o valor de int do departmentEntint.getId() para String
		textId.setText(String.valueOf(departmentEntity.getId()));

		textName.setText(departmentEntity.getName());
	}

}
