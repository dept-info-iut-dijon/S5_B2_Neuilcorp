const uploadForm = document.getElementById("uploadForm");
const image1Input = document.getElementById("image1");
const image2Input = document.getElementById("image2");
const uploadedImage1 = document.getElementById("uploaded-image-1");
const uploadedImage2 = document.getElementById("uploaded-image-2");
const imageContainer = document.getElementById("image-container");
const differencesList = document.getElementById("differences-list");
const submitDifferencesButton = document.getElementById("submit-differences");
const redCircle = document.getElementById("red-circle");

document.addEventListener('wheel', function(event) {
    if (event.ctrlKey || event.metaKey) {
        event.preventDefault();
    }
}, { passive: false });

document.addEventListener('keydown', function(event) {
    if ((event.ctrlKey || event.metaKey) && 
        (event.key === '+' || event.key === '-' || event.key === '0')) {
        event.preventDefault();
    }
});

function moveRedCircle(event) {
    redCircle.style.left = `${event.pageX - 25}px`;
    redCircle.style.top = `${event.pageY - 25}px`;
    redCircle.style.display = 'block';
}

function hideRedCircle() {
    redCircle.style.display = 'none';
}

uploadedImage1.addEventListener('mousemove', moveRedCircle);
uploadedImage1.addEventListener('mouseleave', hideRedCircle);
uploadedImage2.addEventListener('mousemove', hideRedCircle);

let differences = []; // Liste des différences
const markers = []; // Stocke les marqueurs pour suppression

// Charger les images
uploadForm.addEventListener("submit", (event) => {
    event.preventDefault();

    const file1 = image1Input.files[0];
    const file2 = image2Input.files[0];

    if (file1 && file2) {
        const reader1 = new FileReader();
        const reader2 = new FileReader();

        reader1.onload = (e) => {
            uploadedImage1.src = e.target.result;
            imageContainer.style.display = "flex";
        };
        reader2.onload = (e) => {
            uploadedImage2.src = e.target.result;
            imageContainer.style.display = "flex";
        };

        reader1.readAsDataURL(file1);
        reader2.readAsDataURL(file2);

        submitDifferencesButton.style.display = "inline-block";
    }
});

// Ajouter une différence
uploadedImage1.addEventListener("click", (event) => {
    const rect1 = uploadedImage1.getBoundingClientRect();
    const scaleX = uploadedImage1.naturalWidth / uploadedImage1.width;
    const scaleY = uploadedImage1.naturalHeight / uploadedImage1.height;

    const x = (event.clientX - rect1.left) * scaleX;
    const y = (event.clientY - rect1.top) * scaleY;

    // Vérification de la proximité avec une différence existante
    const minDistance = 80;
    const isNearExistingDifference = differences.some((diff) => {
        const dx = (diff.x - x) ** 2;
        const dy = (diff.y - y) ** 2;
        return Math.sqrt(dx + dy) < minDistance;
    });

    if (isNearExistingDifference) {
        return;
    }

    // Cercle rouge sur les deux images
    const marker1 = document.createElement("div");
    marker1.classList.add("difference");
    marker1.style.left = `${event.clientX - rect1.left - 25}px`;
    marker1.style.top = `${event.clientY - rect1.top - 25}px`;
    uploadedImage1.parentNode.appendChild(marker1);

    const rect2 = uploadedImage2.getBoundingClientRect();
    const marker2 = document.createElement("div");
    marker2.classList.add("difference");
    marker2.style.left = `${(x / scaleX) - 25}px`;
    marker2.style.top = `${(y / scaleY) - 25}px`;
    uploadedImage2.parentNode.appendChild(marker2);

    markers.push({ marker1, marker2 });

    differences.push({ x, y });

    updateDifferencesList();
});

// Mettre à jour la liste des différences
function updateDifferencesList() {
    differencesList.innerHTML = "";
    differences.forEach((diff, index) => {
        const item = document.createElement("div");
        item.classList.add("difference-item");
        item.innerHTML = `
            Différence ${index + 1} : (x: ${diff.x.toFixed(1)}, y: ${diff.y.toFixed(1)})
            <span class="delete-difference" data-index="${index}">✖</span>
        `;
        item.querySelector(".delete-difference").addEventListener("click", () => {
            differences.splice(index, 1);
            const { marker1, marker2 } = markers.splice(index, 1)[0];
            marker1.remove();
            marker2.remove();
            updateDifferencesList();
        });
        differencesList.appendChild(item);
    });
}

// Envoyer les différences au serveur
submitDifferencesButton.addEventListener("click", () => {
    const file1 = image1Input.files[0];
    const file2 = image2Input.files[0];
    const name = nameInput.value.trim();
    const differencesString = JSON.stringify(differences);

    if (!file1 || !file2) {
        alert("Veuillez sélectionner deux fichiers d'image.");
        return;
    }

    if (!name) {
        alert("Veuillez entrer un nom pour la paire d'images.");
        return;
    }

    if (differences.length === 0) {
        alert("Veuillez sélectionner au moins une différence.");
        return;
    }

    const formData = new FormData();
    formData.append("image1", file1);
    formData.append("image2", file2);
    formData.append("name", name);
    formData.append("differences", differencesString);

    fetch("http://localhost:5195/ImageControlleur/compare", {
        method: "POST",
        body: formData,
    })
        .then((response) => {
            if (!response.ok) throw new Error("Erreur lors de l'envoi");
            return response.text();
        })
        .then(() => {
            alert("Images et différences envoyées avec succès !");
            window.location.reload(); // Réinitialiser la page
        })
        .catch((error) => {
            console.error("Erreur d'envoi des données :", error.message);
            alert("Erreur d'envoi des données : " + error.message);
        });
});