const buttons = document.querySelectorAll('.custom-button');
    buttons.forEach(button => {
    button.addEventListener('click', () => {
        buttons.forEach(btn => btn.classList.remove('checked'));
        button.classList.add('checked');
        console.log('Checked button value: ' + button.value);
        });
    });

function submitForm() {
    const x = document.querySelector('.custom-button.checked').value;
    const y = document.getElementById('y').value.trim();
    const rElements = document.getElementsByName('r');
    let r;
    for (let i = 0; i < rElements.length; i++) {
        if (rElements[i].checked) {
            r = rElements[i].value;
            break;
        }
    }

    // Validate input
    if (!validateInput(x, y, r)) {
        alert("Некорректные данные" + x + y + r)
        return;
    }

    // Prepare data for POST request
    const data = new URLSearchParams();
    data.append('x', x);
    data.append('y', y);
    data.append('r', r);

    // Send POST request using Fetch API
    fetch("http://localhost:21038/fcgi-bin/app.jar", {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
    })
        .then(response => response.json())
        .then(result => {
            if (result.error) {
                alert("Ошибка сервера: " + result.error);
            } else {
                addResultToTable({
                    x: x,
                    y: y,
                    r: r,
                    hit: result.result,
                    timestamp: new Date().toLocaleString(),
                    executionTime: result.executionTime + ' ms' // Use executionTime from backend
                });
            }
        })
        .catch(error => {
            alert("Ошибка запроса: " + error);
        });
}

function validateInput(x, y, r) {
    const xValue = parseFloat(x);
    const yValue = parseFloat(y.replace(',', '.'));
    const rValue = parseFloat(r);

    if (isNaN(xValue) || isNaN(yValue) || isNaN(rValue)) return false;
    if (yValue < -3 || yValue > 5) return false; // Ensure correct range for y
    return [1, 2, 3, 4, 5].includes(rValue) && [-4, -3, -2, -1, 0, 1, 2, 3, 4].includes(xValue);
}





function addResultToTable(x, y, r, hit, currentTime, elapsedTime) {
    const resultBody = document.getElementById("result-body");
    const newRow = document.createElement("tr");

    const xCell = document.createElement("td");
    xCell.textContent = x;

    const yCell = document.createElement("td");
    yCell.textContent = y;

    const rCell = document.createElement("td");
    rCell.textContent = r;

    const resultCell = document.createElement("td");
    resultCell.textContent = hit ? "Попал" : "Промах";

    const currentTimeCell = document.createElement("td");
    currentTimeCell.textContent = currentTime;

    const elapsedTimeCell = document.createElement("td");
    elapsedTimeCell.textContent = elapsedTime + " ms";

    newRow.appendChild(xCell);
    newRow.appendChild(yCell);
    newRow.appendChild(rCell);
    newRow.appendChild(resultCell);
    newRow.appendChild(currentTimeCell);
    newRow.appendChild(elapsedTimeCell);

    resultBody.appendChild(newRow);
}

