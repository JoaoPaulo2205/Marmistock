package br.com.cerradomarmitas.view;

import br.com.cerradomarmitas.models.Marmita;

import javax.swing.*;

public class SupplyEdit {
    private final SupplyRegister formulario;

    public SupplyEdit() {
        this(null, null);
    }

    public SupplyEdit(Marmita marmita, Runnable aoSalvar) {
        formulario = new SupplyRegister(marmita, aoSalvar);
    }

    public JPanel getPainel() {
        return formulario.getPainel();
    }
}
