import React, { useState, useContext } from 'react';
import ItemService from '../services/itemService';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';

const AddItem = () => {
    const navigate = useNavigate();
    const [item, setItem] = useState({
        title: '',
        description: '',
        location: '',
        found: false
    });
    const [selectedFile, setSelectedFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const { user } = useContext(AuthContext);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setItem({ ...item, [name]: type === 'checkbox' ? checked : value });
    };

    const handleFileChange = (e) => {
        setSelectedFile(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            if (!user || !user.id) {
                toast.error("You must be logged in to report an item.");
                setLoading(false);
                return;
            }

            const itemToSubmit = { ...item, reporterId: user.id };
            const response = await ItemService.createItem(itemToSubmit);
            const newItemId = response.data.id;

            if (selectedFile) {
                await ItemService.uploadImage(newItemId, selectedFile);
            }

            toast.success("Item reported successfully!");
            navigate('/');
        } catch (err) {
            console.error(err);
            toast.error("Failed to add item. Please check the backend connection.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="form-card">
            <h2>Report Lost or Found Item</h2>
            <form onSubmit={handleSubmit}>
                <div className="input-group">
                    <label>Item Title</label>
                    <input name="title" placeholder="e.g., iPhone 13, Keys" onChange={handleChange} required />
                </div>

                <div className="input-group">
                    <label>Description</label>
                    <textarea name="description" rows="3" onChange={handleChange} required />
                </div>

                <div className="input-group">
                    <label>Location</label>
                    <input name="location" placeholder="Where was it seen?" onChange={handleChange} required />
                </div>

                <div className="input-group">
                    <label>Your Contact Number</label>
                    <input name="contactInfo" placeholder="Phone number" onChange={handleChange} required />
                </div>

                <div className="input-group">
                    <label>Item Type</label>
                    <div className="toggle-group">
                        <button 
                            type="button" 
                            className={`toggle-btn ${!item.found ? 'active lost' : ''}`} 
                            onClick={() => setItem({ ...item, found: false })}
                        >
                            I Lost This
                        </button>
                        <button 
                            type="button" 
                            className={`toggle-btn ${item.found ? 'active found' : ''}`} 
                            onClick={() => setItem({ ...item, found: true })}
                        >
                            I Found This
                        </button>
                    </div>
                </div>

                <div className="input-group">
                    <label>Upload Photo (Optional)</label>
                    <input type="file" accept="image/*" onChange={handleFileChange} />
                </div>

                <button type="submit" className="primary-btn" disabled={loading}>
                    {loading ? 'Submitting...' : 'Post Item'}
                </button>
            </form>
        </div>
    );
};

export default AddItem;