// src/main/resources/static/script.js

document.getElementById('checkBtn').addEventListener('click', () => {
    const statement = document.getElementById('statement').value.trim();
    const checkBtn = document.getElementById('checkBtn');

    if (!statement) {
        displayResult('Please enter a statement.', 'error');
        return;
    }

    checkBtn.disabled = true;
    checkBtn.textContent = 'Checking...';

    displayResult('Checking...', 'loading');

    fetch('/api/check', {
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        body: statement
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server error: ${response.statusText}`);
            }
            return response.text();
        })
        .then(data => {
            const normalizedData = data.toLowerCase().trim();
            console.log('API Response:', normalizedData); // For debugging

            if (normalizedData.startsWith('not fake news')) {
                displayResult('Not Fake News', 'not-fake-news');
            } else if (normalizedData.startsWith('fake news')) {
                displayResult('Fake News', 'fake-news');
            } else {
                displayResult(data, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            displayResult('An error occurred while checking the fact.', 'error');
        })
        .finally(() => {
            // Re-enable the button
            checkBtn.disabled = false;
            checkBtn.textContent = 'Check Fact';
        });
});

/**
 * @param {string} message - The message to display.
 * @param {string} type - The type of message ('fake-news', 'not-fake-news', 'error', 'loading').
 */
function displayResult(message, type) {
    const resultDiv = document.getElementById('result');
    const resultText = document.getElementById('result-text');
    const spinner = document.getElementById('spinner');

    if (type === 'loading') {
        spinner.classList.remove('hidden');
        resultText.classList.add('hidden');
    } else {
        spinner.classList.add('hidden');
        resultText.classList.remove('hidden');
        resultText.textContent = message;
    }

    resultDiv.className = `result ${type}`;
    resultDiv.classList.remove('hidden');

    if (type !== 'loading') {
        resultDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}
