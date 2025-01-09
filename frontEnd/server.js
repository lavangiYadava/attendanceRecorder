const express = require('express')
const app = express()
const axios = require('axios');
const bcrypt = require('bcrypt')
app.use(express.json());

app.set('view-engine', 'ejs')
app.use(express.urlencoded({extended: true}))


/* defining a root URL that listens for GET requests */
app.get('/', (req, res) => {
    res.render('index.ejs', {name: 'Lavangi'})
})

app.get('/login', (req, res) => {
    res.render('login.ejs')
})

app.get('/registerStudent', (req, res) => {
    res.render('registerStudent.ejs')
})
app.get('/registerProfessor', (req, res) => {
    res.render('registerProfessor.ejs')
})

app.post('/registerProfessor', async (req, res) => {
    const { username, email, password} = req.body;

    console.log(username)
    console.log(email)
    console.log(password)

    const professor = {
        username: username,
        email: email,
        password: password,
        ID : 1902
    };

    try {
        const response = await axios.post('http://localhost:8080/professor/newAccount', professor);

        if (response.status === 200) {
            res.redirect('/login');
        } else {
            res.status(response.status).send(response.data);
        }
    } catch (error) {
        console.error(error);
        res.status(500).send("An error occurred while creating your account.");
    }
});

app.listen(8080)