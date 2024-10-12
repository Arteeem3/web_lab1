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
        alert("Некорректные данные: x=" + x + ", y=" + y + ", r=" + r);
        return;
    }

    // Prepare data for POST request
    const data = new URLSearchParams();
    data.append('x', x);
    data.append('y', y);
    data.append('r', r);

    console.log("Data being sent to server:", { x, y, r });

    // Send POST request using Fetch API
fetch('http://localhost:21038/fcgi-bin/app.jar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: data.toString(),
    })
        .then(response => console.log(response))
        .then(response => response.json())
        .then(result => {
            if (result.error) {
                alert("Ошибка сервера: " + result.error);
            } else {
                updateResults({
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

function updateResults(result) {
    const table = document.getElementById('resultTable');
    const row = table.insertRow(1);
    row.insertCell(0).innerText = result.x;
    row.insertCell(1).innerText = result.y;
    row.insertCell(2).innerText = result.r;
    row.insertCell(3).innerText = result.hit ? 'Попадание' : 'Промах';
    row.insertCell(4).innerText = result.timestamp;
    row.insertCell(5).innerText = result.executionTime;
}



