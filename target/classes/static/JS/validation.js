// Form validation script
(function () {
    'use strict'
    
    // Fetch all forms that need validation
    const forms = document.querySelectorAll('.needs-validation');
    
    // Loop over them and prevent submission
    Array.from(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            // Sprawdź wartości przy próbie wysłania formularza
            const nameInput = document.getElementById('name');
            const quantityInput = document.getElementById('quantity');
            const priceInput = document.getElementById('price');
            const weightInput = document.getElementById('weight');
            const imageInput = document.getElementById('image');
            const maxCapacityInput = document.getElementById('maxCapacity');
            
            let isValid = true;
            
            // Sprawdź name (dla Zone)
            if (nameInput && nameInput.value.trim() === '') {
                nameInput.classList.add('is-invalid');
                nameInput.style.borderColor = '#dc3545';
                nameInput.style.color = '#dc3545';
                isValid = false;
            } else if (nameInput) {
                nameInput.classList.remove('is-invalid');
                nameInput.style.borderColor = '';
                nameInput.style.color = '';
            }
            
            // Sprawdź quantity (dla Product)
            if (quantityInput && (isNaN(parseFloat(quantityInput.value)) || parseFloat(quantityInput.value) <= 0)) {
                quantityInput.classList.add('is-invalid');
                quantityInput.style.borderColor = '#dc3545';
                quantityInput.style.color = '#dc3545';
                isValid = false;
            } else if (quantityInput) {
                quantityInput.classList.remove('is-invalid');
                quantityInput.style.borderColor = '';
                quantityInput.style.color = '';
            }
            
            // Sprawdź price (dla Product)
            if (priceInput && (isNaN(parseFloat(priceInput.value)) || parseFloat(priceInput.value) <= 0)) {
                priceInput.classList.add('is-invalid');
                priceInput.style.borderColor = '#dc3545';
                priceInput.style.color = '#dc3545';
                isValid = false;
            } else if (priceInput) {
                priceInput.classList.remove('is-invalid');
                priceInput.style.borderColor = '';
                priceInput.style.color = '';
            }
            
            // Sprawdź weight (dla Product)
            if (weightInput && (isNaN(parseFloat(weightInput.value)) || parseFloat(weightInput.value) <= 0)) {
                weightInput.classList.add('is-invalid');
                weightInput.style.borderColor = '#dc3545';
                weightInput.style.color = '#dc3545';
                isValid = false;
            } else if (weightInput) {
                weightInput.classList.remove('is-invalid');
                weightInput.style.borderColor = '';
                weightInput.style.color = '';
            }
            
            // Sprawdź maxCapacity (dla Zone)
            if (maxCapacityInput && (isNaN(parseFloat(maxCapacityInput.value)) || parseFloat(maxCapacityInput.value) <= 0)) {
                maxCapacityInput.classList.add('is-invalid');
                maxCapacityInput.style.borderColor = '#dc3545';
                maxCapacityInput.style.color = '#dc3545';
                isValid = false;
            } else if (maxCapacityInput) {
                maxCapacityInput.classList.remove('is-invalid');
                maxCapacityInput.style.borderColor = '';
                maxCapacityInput.style.color = '';
            }
            
            // Sprawdź image (dla Product)
            if (imageInput && imageInput.hasAttribute('required') && (!imageInput.files || !imageInput.files[0])) {
                imageInput.classList.add('is-invalid');
                isValid = false;
            } else if (imageInput) {
                imageInput.classList.remove('is-invalid');
            }
            
            if (!isValid || !form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
})();

// Dodatkowe funkcje do obsługi formularza
document.addEventListener('DOMContentLoaded', function () {
    // Usuń domyślne ptaszki walidacji
    const form = document.querySelector('.needs-validation');
    if (form) {
        form.classList.remove('was-validated');
    }
    
    // Znajdź pola formularza
    const nameInput = document.getElementById('name');
    const quantityInput = document.getElementById('quantity');
    const priceInput = document.getElementById('price');
    const weightInput = document.getElementById('weight');
    const imageInput = document.getElementById('image');
    const maxCapacityInput = document.getElementById('maxCapacity');
    
    // Wyczyść domyślne wartości jeśli są równe 0
    if (quantityInput && quantityInput.value === '0') {
        quantityInput.value = '';
    }
    if (priceInput && priceInput.value === '0') {
        priceInput.value = '';
    }
    if (weightInput && weightInput.value === '0') {
        weightInput.value = '';
    }
    if (maxCapacityInput && maxCapacityInput.value === '0') {
        maxCapacityInput.value = '';
    }
    
    // Funkcja do walidacji pól numerycznych
    function validateNumericField(input) {
        if (input) {
            input.addEventListener('input', function() {
                if (isNaN(parseFloat(this.value)) || parseFloat(this.value) <= 0) {
                    this.classList.add('is-invalid');
                    this.style.borderColor = '#dc3545';
                    this.style.color = '#dc3545';
                } else {
                    this.classList.remove('is-invalid');
                    this.style.borderColor = '';
                    this.style.color = '';
                }
            });
        }
    }
    
    // Dodaj nasłuchiwanie na zmiany w polu name (dla Zone)
    if (nameInput) {
        nameInput.addEventListener('input', function() {
            if (this.value.trim() === '') {
                this.classList.add('is-invalid');
                this.style.borderColor = '#dc3545';
                this.style.color = '#dc3545';
            } else {
                this.classList.remove('is-invalid');
                this.style.borderColor = '';
                this.style.color = '';
            }
        });
    }
    
    // Dodaj nasłuchiwanie na zmiany w polach numerycznych
    validateNumericField(quantityInput);
    validateNumericField(priceInput);
    validateNumericField(weightInput);
    validateNumericField(maxCapacityInput);
    
    // Dodaj nasłuchiwanie na zmiany w polu image (dla Product)
    if (imageInput) {
        imageInput.addEventListener('change', function() {
            if (this.hasAttribute('required') && (!this.files || !this.files[0])) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });
    }
    
    // Toast notifications
    const successToast = document.getElementById('successToast');
    const errorToast = document.getElementById('errorToast');
    
    if (successToast && successToast.querySelector('.toast-body span').textContent.trim() !== '') {
        new bootstrap.Toast(successToast).show();
    }
    
    if (errorToast && errorToast.querySelector('.toast-body span').textContent.trim() !== '') {
        new bootstrap.Toast(errorToast).show();
    }
    
    // Modal handling for role editing
    const editRolesModal = document.getElementById('editRolesModal');
    if (editRolesModal) {
        editRolesModal.addEventListener('show.bs.modal', function(event) {
            const button = event.relatedTarget;
            const userId = button.getAttribute('data-user-id');
            const userRole = button.getAttribute('data-user-role');
            this.querySelector('#userId').value = userId;
            const radioButton = this.querySelector(`input[type="radio"][value="${userRole}"]`);
            if (radioButton) {
                radioButton.checked = true;
            }
        });
    }
});
